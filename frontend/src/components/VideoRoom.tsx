import { useEffect, useRef } from 'react'
import { Button, Space, Tooltip } from 'antd'
import {
  AudioOutlined,
  AudioMutedOutlined,
  VideoCameraOutlined,
  VideoCameraAddOutlined,
  PhoneOutlined,
} from '@ant-design/icons'
import { useRTC } from '@/hooks/useRTC'

interface VideoRoomProps {
  consultationId: number
  onLeave?: () => void
}

/**
 * 视频房间组件
 * _Requirements: 4.7_
 */
export const VideoRoom: React.FC<VideoRoomProps> = ({ consultationId, onLeave }) => {
  const {
    isConnected,
    isAudioEnabled,
    isVideoEnabled,
    localStream,
    remoteStream,
    joinRoom,
    leaveRoom,
    toggleMic,
    toggleCamera,
  } = useRTC(consultationId)

  const localVideoRef = useRef<HTMLVideoElement>(null)
  const remoteVideoRef = useRef<HTMLVideoElement>(null)

  // 绑定本地视频流
  useEffect(() => {
    if (localVideoRef.current && localStream) {
      localVideoRef.current.srcObject = localStream
    }
  }, [localStream])

  // 绑定远程视频流
  useEffect(() => {
    if (remoteVideoRef.current && remoteStream) {
      remoteVideoRef.current.srcObject = remoteStream
    }
  }, [remoteStream])

  const handleLeave = async () => {
    await leaveRoom()
    onLeave?.()
  }

  const handleJoin = async () => {
    await joinRoom()
  }

  const handleToggleAudio = () => {
    toggleMic()
  }

  const handleToggleVideo = () => {
    toggleCamera()
  }

  return (
    <div style={{ 
      display: 'flex', 
      flexDirection: 'column', 
      height: '100%',
      background: '#1a1a1a',
      borderRadius: 8,
      overflow: 'hidden',
    }}>
      {/* 视频区域 */}
      <div style={{ 
        flex: 1, 
        display: 'flex', 
        position: 'relative',
        padding: 16,
        gap: 16,
      }}>
        {/* 远程视频（大） */}
        <div style={{ 
          flex: 1, 
          background: '#2a2a2a', 
          borderRadius: 8,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}>
          {remoteStream ? (
            <video
              ref={remoteVideoRef}
              autoPlay
              playsInline
              style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: 8 }}
            />
          ) : (
            <div style={{ color: '#666', fontSize: 16 }}>
              {isConnected ? '等待对方加入...' : '点击下方按钮加入房间'}
            </div>
          )}
        </div>

        {/* 本地视频（小） */}
        <div style={{ 
          position: 'absolute',
          right: 32,
          bottom: 32,
          width: 200,
          height: 150,
          background: '#2a2a2a',
          borderRadius: 8,
          overflow: 'hidden',
          border: '2px solid #333',
        }}>
          {localStream ? (
            <video
              ref={localVideoRef}
              autoPlay
              playsInline
              muted
              style={{ width: '100%', height: '100%', objectFit: 'cover' }}
            />
          ) : (
            <div style={{ 
              width: '100%', 
              height: '100%', 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'center',
              color: '#666',
              fontSize: 12,
            }}>
              本地视频
            </div>
          )}
        </div>
      </div>

      {/* 控制栏 */}
      <div style={{ 
        padding: 16, 
        background: '#2a2a2a',
        display: 'flex',
        justifyContent: 'center',
      }}>
        <Space size="large">
          {!isConnected ? (
            <Button 
              type="primary" 
              size="large"
              icon={<VideoCameraOutlined />}
              onClick={handleJoin}
            >
              加入房间
            </Button>
          ) : (
            <>
              <Tooltip title={isAudioEnabled ? '关闭麦克风' : '开启麦克风'}>
                <Button
                  shape="circle"
                  size="large"
                  icon={isAudioEnabled ? <AudioOutlined /> : <AudioMutedOutlined />}
                  onClick={handleToggleAudio}
                  style={{ 
                    background: isAudioEnabled ? '#52c41a' : '#ff4d4f',
                    borderColor: 'transparent',
                    color: '#fff',
                  }}
                />
              </Tooltip>

              <Tooltip title={isVideoEnabled ? '关闭摄像头' : '开启摄像头'}>
                <Button
                  shape="circle"
                  size="large"
                  icon={isVideoEnabled ? <VideoCameraOutlined /> : <VideoCameraAddOutlined />}
                  onClick={handleToggleVideo}
                  style={{ 
                    background: isVideoEnabled ? '#52c41a' : '#ff4d4f',
                    borderColor: 'transparent',
                    color: '#fff',
                  }}
                />
              </Tooltip>

              <Tooltip title="挂断">
                <Button
                  shape="circle"
                  size="large"
                  danger
                  icon={<PhoneOutlined style={{ transform: 'rotate(135deg)' }} />}
                  onClick={handleLeave}
                />
              </Tooltip>
            </>
          )}
        </Space>
      </div>
    </div>
  )
}

export default VideoRoom
