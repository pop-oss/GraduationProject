/**
 * RTC 服务
 * _Requirements: 4.6, 4.7, 4.8_
 */

import { get, post } from './http';

export interface RTCToken {
  token: string;
  roomId: string;
  userId: number;
  expiresAt: number;
}

export interface Device {
  deviceId: string;
  label: string;
  kind: string;
}

/**
 * RTC 服务
 * _Requirements: 4.6, 4.7, 4.8_
 */
export const rtcService = {
  /**
   * 获取 RTC Token
   * _Requirements: 4.6_
   */
  getToken: async (consultationId: number): Promise<RTCToken> => {
    const response = await get<RTCToken>(`/rtc/token/${consultationId}`);
    return response.data;
  },

  /**
   * 加入房间
   */
  joinRoom: async (consultationId: number): Promise<void> => {
    await post(`/rtc/join/${consultationId}`);
  },

  /**
   * 离开房间
   */
  leaveRoom: async (consultationId: number): Promise<void> => {
    await post(`/rtc/leave/${consultationId}`);
  },

  /**
   * 获取可用设备列表
   * _Requirements: 4.7_
   */
  getDevices: async (): Promise<{ mics: Device[]; cameras: Device[] }> => {
    const devices = await navigator.mediaDevices.enumerateDevices();
    const mics = devices
      .filter((d) => d.kind === 'audioinput')
      .map((d) => ({
        deviceId: d.deviceId,
        label: d.label || `麦克风 ${d.deviceId.slice(0, 8)}`,
        kind: d.kind,
      }));
    const cameras = devices
      .filter((d) => d.kind === 'videoinput')
      .map((d) => ({
        deviceId: d.deviceId,
        label: d.label || `摄像头 ${d.deviceId.slice(0, 8)}`,
        kind: d.kind,
      }));
    return { mics, cameras };
  },
};

export default rtcService;
