/**
 * 医生视频房间页面
 * _Requirements: 6.6_
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
  UserAddOutlined,
  FileTextOutlined,
} from '@ant-design/icons';
import { useRTC } from '@/hooks/useRTC';
import { consultationService } from '@/services/consultation';
import RoomLayout from '@/components/RoomLayout';
import DeviceSelector from '@/components/DeviceSelector';
import NetworkIndicator from '@/components/NetworkIndicator';
import ConfirmButton from '@/components/ConfirmButton';

/**
 * 医生视频房间页面
 * _Requirements: 6.6_
 */
const DoctorConsultationRoom: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [joining, setJoining] = useState(true);

  const {
    isAudioEnabled,
    isVideoEnabled,
    localStream,
    remoteStream,
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

  // 结束问诊
  const handleEndConsultation = async () => {
    if (!id) return;
    try {
      await consultationService.finish(id);
      await leaveRoom();
      message.success('问诊已结束');
      navigate(`/doctor/consultation/${id}`);
    } catch (err) {
      message.error((err as Error).message || '操作失败');
    }
  };

  // 离开房间（不结束问诊）
  const handleLeave = async () => {
    await leaveRoom();
    navigate(`/doctor/consultation/${id}`);
  };

  // 邀请专家（TODO: 实现邀请专家功能）
  const handleInviteExpert = () => {
    message.info('邀请专家功能开发中');
  };

  // 跳转到病历页面
  const handleGoToRecord = () => {
    navigate(`/doctor/medical-record/${id}`);
  };

  // 跳转到开方页面
  const handleGoToPrescription = () => {
    navigate(`/doctor/prescription/${id}`);
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
          <Button icon={<FileTextOutlined />} onClick={handleGoToRecord}>
            填写病历
          </Button>
          <Button icon={<FileTextOutlined />} onClick={handleGoToPrescription}>
            开具处方
          </Button>
        </Space>
      }
      videoArea={
        <div style={{ height: '100%', display: 'flex', gap: 16, padding: 16 }}>
          {/* 本地视频 */}
          <div style={{ flex: 1, position: 'relative', background: '#333', borderRadius: 8 }}>
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
                style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: 8 }}
              />
            ) : (
              <div style={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#999' }}>
                本地视频
              </div>
            )}
            <div style={{ position: 'absolute', bottom: 8, left: 8, color: '#fff', fontSize: 12, background: 'rgba(0,0,0,0.5)', padding: '2px 8px', borderRadius: 4 }}>
              我
            </div>
          </div>
          
          {/* 远程视频 */}
          <div style={{ flex: 1, position: 'relative', background: '#333', borderRadius: 8 }}>
            {remoteStream ? (
              <video
                autoPlay
                playsInline
                ref={(el) => {
                  if (el && remoteStream) {
                    el.srcObject = remoteStream;
                  }
                }}
                style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: 8 }}
              />
            ) : (
              <div style={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#999' }}>
                等待患者加入...
              </div>
            )}
            <div style={{ position: 'absolute', bottom: 8, left: 8, color: '#fff', fontSize: 12, background: 'rgba(0,0,0,0.5)', padding: '2px 8px', borderRadius: 4 }}>
              患者
            </div>
          </div>
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
            shape="circle"
            size="large"
            icon={<UserAddOutlined />}
            onClick={handleInviteExpert}
            title="邀请专家"
          />
          <ConfirmButton
            confirmTitle="结束问诊"
            confirmDescription="确定要结束本次问诊吗？结束后将无法继续视频通话。"
            onConfirm={handleEndConsultation}
            buttonProps={{
              type: 'primary',
              danger: true,
              shape: 'circle',
              size: 'large',
              icon: <PhoneOutlined style={{ transform: 'rotate(135deg)' }} />,
            }}
          >
            结束
          </ConfirmButton>
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
          <div style={{ marginTop: 24 }}>
            <Button block onClick={handleLeave}>
              暂时离开
            </Button>
          </div>
        </div>
      }
    />
  );
};

export default DoctorConsultationRoom;
