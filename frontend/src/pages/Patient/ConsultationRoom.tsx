/**
 * 患者视频房间页面
 * _Requirements: 4.6, 4.7, 4.8_
 */

import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, Space, message, Spin } from 'antd';
import {
  AudioOutlined,
  AudioMutedOutlined,
  VideoCameraOutlined,
  VideoCameraAddOutlined,
  PhoneOutlined,
} from '@ant-design/icons';
import { useRTC } from '@/hooks/useRTC';
import RoomLayout from '@/components/RoomLayout';
import DeviceSelector from '@/components/DeviceSelector';
import NetworkIndicator from '@/components/NetworkIndicator';

/**
 * 患者视频房间页面
 * _Requirements: 4.6, 4.7, 4.8_
 */
const ConsultationRoom: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [joining, setJoining] = useState(true);

  const {
    isAudioEnabled,
    isVideoEnabled,
    localStream,
    networkQuality,
    error,
    devices,
    joinRoom,
    leaveRoom,
    toggleMic,
    toggleCamera,
    switchDevice,
  } = useRTC(Number(id));

  // 加入房间
  useEffect(() => {
    const join = async () => {
      try {
        await joinRoom();
      } catch (err) {
        message.error('加入房间失败');
      } finally {
        setJoining(false);
      }
    };
    join();

    return () => {
      leaveRoom();
    };
  }, [id]);

  // 离开房间
  const handleLeave = async () => {
    await leaveRoom();
    navigate(`/patient/consultation/${id}`);
  };

  if (joining) {
    return (
      <div style={{ height: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#1a1a1a' }}>
        <Spin size="large" tip="正在加入房间..." />
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ height: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#1a1a1a', color: '#fff' }}>
        <div style={{ textAlign: 'center' }}>
          <p>加入房间失败: {error.message}</p>
          <Button type="primary" onClick={() => window.location.reload()}>
            重试
          </Button>
        </div>
      </div>
    );
  }

  return (
    <RoomLayout
      title={`视频问诊 #${id}`}
      headerExtra={
        <Space>
          <NetworkIndicator quality={networkQuality} showText />
        </Space>
      }
      videoArea={
        <div style={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          {localStream ? (
            <video
              autoPlay
              muted
              playsInline
              ref={(el) => {
                if (el && localStream) {
                  el.srcObject = localStream;
                }
              }}
              style={{ maxWidth: '100%', maxHeight: '100%', borderRadius: 8 }}
            />
          ) : (
            <div style={{ color: '#999' }}>等待视频...</div>
          )}
        </div>
      }
      controls={
        <Space size="large">
          <Button
            type={isAudioEnabled ? 'default' : 'primary'}
            danger={!isAudioEnabled}
            shape="circle"
            size="large"
            icon={isAudioEnabled ? <AudioOutlined /> : <AudioMutedOutlined />}
            onClick={() => toggleMic()}
          />
          <Button
            type={isVideoEnabled ? 'default' : 'primary'}
            danger={!isVideoEnabled}
            shape="circle"
            size="large"
            icon={isVideoEnabled ? <VideoCameraOutlined /> : <VideoCameraAddOutlined />}
            onClick={() => toggleCamera()}
          />
          <Button
            type="primary"
            danger
            shape="circle"
            size="large"
            icon={<PhoneOutlined style={{ transform: 'rotate(135deg)' }} />}
            onClick={handleLeave}
          />
        </Space>
      }
      rightPanel={
        <div style={{ padding: 16 }}>
          <h4 style={{ color: '#fff', marginBottom: 16 }}>设备设置</h4>
          <DeviceSelector
            mics={devices.mics}
            cameras={devices.cameras}
            onMicChange={(micId) => switchDevice({ micId })}
            onCameraChange={(cameraId) => switchDevice({ cameraId })}
          />
        </div>
      }
    />
  );
};

export default ConsultationRoom;
