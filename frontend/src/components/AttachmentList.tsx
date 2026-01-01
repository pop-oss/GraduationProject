/**
 * 附件列表组件
 * _Requirements: 13.4_
 */

import React from 'react';
import { List, Button, Image, Typography, Popconfirm } from 'antd';
import {
  FileOutlined,
  FilePdfOutlined,
  FileWordOutlined,
  FileImageOutlined,
  DownloadOutlined,
  DeleteOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import type { UploadedFile } from '@/types';

const { Text } = Typography;

export interface AttachmentListProps {
  /** 附件列表 */
  attachments: UploadedFile[];
  /** 删除回调 */
  onDelete?: (file: UploadedFile) => void;
  /** 是否显示删除按钮 */
  showDelete?: boolean;
  /** 是否显示预览按钮 */
  showPreview?: boolean;
  /** 是否显示下载按钮 */
  showDownload?: boolean;
}

/**
 * 获取文件图标
 */
const getFileIcon = (file: UploadedFile) => {
  const type = file.type || '';
  const name = file.name.toLowerCase();

  if (type.startsWith('image/') || /\.(jpg|jpeg|png|gif|webp)$/.test(name)) {
    return <FileImageOutlined style={{ fontSize: 24, color: '#1890ff' }} />;
  }
  if (type === 'application/pdf' || name.endsWith('.pdf')) {
    return <FilePdfOutlined style={{ fontSize: 24, color: '#ff4d4f' }} />;
  }
  if (type.includes('word') || /\.(doc|docx)$/.test(name)) {
    return <FileWordOutlined style={{ fontSize: 24, color: '#1890ff' }} />;
  }
  return <FileOutlined style={{ fontSize: 24, color: '#999' }} />;
};

/**
 * 格式化文件大小
 */
const formatFileSize = (size: number) => {
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${(size / 1024 / 1024).toFixed(1)} MB`;
};

/**
 * 判断是否为图片
 */
const isImage = (file: UploadedFile) => {
  const type = file.type || '';
  const name = file.name.toLowerCase();
  return type.startsWith('image/') || /\.(jpg|jpeg|png|gif|webp)$/.test(name);
};

/**
 * 附件列表组件
 * 支持预览、下载、删除
 * _Requirements: 13.4_
 */
export const AttachmentList: React.FC<AttachmentListProps> = ({
  attachments,
  onDelete,
  showDelete = false,
  showPreview = true,
  showDownload = true,
}) => {
  const handleDownload = (file: UploadedFile) => {
    const link = document.createElement('a');
    link.href = file.url;
    link.download = file.name;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <List
      dataSource={attachments}
      renderItem={(file) => (
        <List.Item
          actions={[
            showPreview && isImage(file) && (
              <Image
                key="preview"
                src={file.url}
                width={0}
                height={0}
                style={{ display: 'none' }}
                preview={{
                  visible: false,
                  src: file.url,
                }}
              />
            ),
            showPreview && isImage(file) && (
              <Button
                key="preview-btn"
                type="link"
                size="small"
                icon={<EyeOutlined />}
                onClick={() => {
                  window.open(file.url, '_blank');
                }}
              >
                预览
              </Button>
            ),
            showDownload && (
              <Button
                key="download"
                type="link"
                size="small"
                icon={<DownloadOutlined />}
                onClick={() => handleDownload(file)}
              >
                下载
              </Button>
            ),
            showDelete && onDelete && (
              <Popconfirm
                key="delete"
                title="确定删除此附件？"
                onConfirm={() => onDelete(file)}
                okText="确定"
                cancelText="取消"
              >
                <Button type="link" size="small" danger icon={<DeleteOutlined />}>
                  删除
                </Button>
              </Popconfirm>
            ),
          ].filter(Boolean)}
        >
          <List.Item.Meta
            avatar={getFileIcon(file)}
            title={file.name}
            description={<Text type="secondary">{formatFileSize(file.size)}</Text>}
          />
        </List.Item>
      )}
    />
  );
};

export default AttachmentList;
