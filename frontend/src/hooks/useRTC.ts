import { useState, useCallback, useRef, useEffect } from 'react';
import { rtcService, RTCToken, Device } from '@/services/rtc';
import { message } from 'antd';

export interface RTCState {
  isConnected: boolean;
  isAudioEnabled: boolean;
  isVideoEnabled: boolean;
  localStream: MediaStream | null;
  remoteStream: MediaStream | null;
  networkQuality: number; // 0-5, 0 表示未知
  error: Error | null;
}

export interface RTCConfig {
  consultationId: string | number;
}

/**
 * RTC Hook
 * _Requirements: 4.6, 4.7, 4.8_
 */
export const useRTC = (consultationId: number | string) => {
  const [state, setState] = useState<RTCState>({
    isConnected: false,
    isAudioEnabled: true,
    isVideoEnabled: true,
    localStream: null,
    remoteStream: null,
    networkQuality: 0,
    error: null,
  });

  const [devices, setDevices] = useState<{ mics: Device[]; cameras: Device[] }>({
    mics: [],
    cameras: [],
  });

  const peerConnectionRef = useRef<RTCPeerConnection | null>(null);
  const localStreamRef = useRef<MediaStream | null>(null);

  /**
   * 获取设备列表
   * _Requirements: 4.7_
   */
  const getDevices = useCallback(async () => {
    try {
      const deviceList = await rtcService.getDevices();
      setDevices(deviceList);
      return deviceList;
    } catch (error) {
      console.error('获取设备列表失败', error);
      return { mics: [], cameras: [] };
    }
  }, []);

  /**
   * 获取本地媒体流
   */
  const getLocalStream = useCallback(
    async (options?: { micId?: string; cameraId?: string }) => {
      try {
        const constraints: MediaStreamConstraints = {
          video: options?.cameraId ? { deviceId: { exact: options.cameraId } } : true,
          audio: options?.micId ? { deviceId: { exact: options.micId } } : true,
        };
        const stream = await navigator.mediaDevices.getUserMedia(constraints);
        localStreamRef.current = stream;
        setState((prev) => ({ ...prev, localStream: stream, error: null }));
        return stream;
      } catch (error) {
        const err = error as Error;
        message.error('无法获取摄像头/麦克风权限');
        setState((prev) => ({ ...prev, error: err }));
        throw error;
      }
    },
    []
  );

  /**
   * 加入房间
   * _Requirements: 4.6_
   */
  const joinRoom = useCallback(
    async (config?: RTCConfig) => {
      const roomId = config?.consultationId || consultationId;
      try {
        // 获取 RTC Token
        const tokenData: RTCToken = await rtcService.getToken(Number(roomId));

        // 获取本地流
        await getLocalStream();

        // 通知后端加入房间
        await rtcService.joinRoom(Number(roomId));

        setState((prev) => ({ ...prev, isConnected: true, error: null }));
        message.success('已加入视频房间');

        return tokenData;
      } catch (error) {
        const err = error as Error;
        message.error('加入房间失败');
        setState((prev) => ({ ...prev, error: err }));
        throw error;
      }
    },
    [consultationId, getLocalStream]
  );

  /**
   * 离开房间
   */
  const leaveRoom = useCallback(async () => {
    try {
      // 停止本地流
      if (localStreamRef.current) {
        localStreamRef.current.getTracks().forEach((track) => track.stop());
        localStreamRef.current = null;
      }

      // 关闭 PeerConnection
      if (peerConnectionRef.current) {
        peerConnectionRef.current.close();
        peerConnectionRef.current = null;
      }

      // 通知后端离开房间
      await rtcService.leaveRoom(Number(consultationId));

      setState({
        isConnected: false,
        isAudioEnabled: true,
        isVideoEnabled: true,
        localStream: null,
        remoteStream: null,
        networkQuality: 0,
        error: null,
      });

      message.info('已离开视频房间');
    } catch (error) {
      console.error('离开房间失败', error);
    }
  }, [consultationId]);

  /**
   * 切换麦克风
   * _Requirements: 4.7_
   */
  const toggleMic = useCallback((on?: boolean) => {
    if (localStreamRef.current) {
      const audioTrack = localStreamRef.current.getAudioTracks()[0];
      if (audioTrack) {
        const newState = on !== undefined ? on : !audioTrack.enabled;
        audioTrack.enabled = newState;
        setState((prev) => ({ ...prev, isAudioEnabled: newState }));
      }
    }
  }, []);

  /**
   * 切换摄像头
   * _Requirements: 4.7_
   */
  const toggleCamera = useCallback((on?: boolean) => {
    if (localStreamRef.current) {
      const videoTrack = localStreamRef.current.getVideoTracks()[0];
      if (videoTrack) {
        const newState = on !== undefined ? on : !videoTrack.enabled;
        videoTrack.enabled = newState;
        setState((prev) => ({ ...prev, isVideoEnabled: newState }));
      }
    }
  }, []);

  /**
   * 切换设备
   * _Requirements: 4.7_
   */
  const switchDevice = useCallback(
    async (options: { micId?: string; cameraId?: string }) => {
      try {
        // 停止当前流
        if (localStreamRef.current) {
          localStreamRef.current.getTracks().forEach((track) => track.stop());
        }
        // 获取新流
        await getLocalStream(options);
        message.success('设备切换成功');
      } catch (error) {
        message.error('设备切换失败');
      }
    },
    [getLocalStream]
  );

  // 组件卸载时清理
  useEffect(() => {
    return () => {
      if (localStreamRef.current) {
        localStreamRef.current.getTracks().forEach((track) => track.stop());
      }
      if (peerConnectionRef.current) {
        peerConnectionRef.current.close();
      }
    };
  }, []);

  // 初始化时获取设备列表
  useEffect(() => {
    getDevices();
  }, [getDevices]);

  return {
    ...state,
    devices,
    // 方法
    joinRoom,
    leaveRoom,
    toggleMic,
    toggleCamera,
    switchDevice,
    getDevices,
    // 兼容旧接口
    join: joinRoom,
    leave: leaveRoom,
    toggleAudio: toggleMic,
    toggleVideo: toggleCamera,
  };
};

export default useRTC;
