/**
 * 附件上传组件
 * _Requirements: 13.1, 13.2, 13.3, 13.4_
 */

import React, { useState } from 'react';
import { Upload, Button, message, Progress } from 'antd';
import { UploadOutlined, PlusOutlined } from '@ant-design/icons';
import type { UploadFile, UploadProps } from 'antd/es/upload';
import type { UploadedFile } from '@/types';
import { getAccessToken } from '@/utils/storage';

export interface AttachmentUploaderProps {
  /** 已上传文件列表 */
  value?: UploadedFile[];
  /** 文件变化回调 */
  onChange?: (files: UploadedFile[]) => void;
  /** 最大文件数量 */
  maxCount?: number;
  /** 最大文件大小（MB） */
  maxSize?: number;
  /** 允许的文件类型 */
  accept?: string;
  /** 是否禁用 */
  disabled?: boolean;
  /** 上传按钮文字 */
  buttonText?: string;
  /** 是否显示为卡片样式 */
  listType?: 'text' | 'picture' | 'picture-card';
}

/**
 * 附件上传组件
 * 实现文件类型/大小校验和上传进度显示
 * _Requirements: 13.1, 13.2, 13.3, 13.4_
 */
export const AttachmentUploader: React.FC<AttachmentUploaderProps> = ({
  value = [],
  onChange,
  maxCount = 5,
  maxSize = 10,
  accept = 'image/*,.pdf,.doc,.docx',
  disabled = false,
  buttonText = '上传文件',
  listType = 'text',
}) => {
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);

  // 转换为 Upload 组件需要的格式
  const fileList: UploadFile[] = value.map((file) => ({
    uid: String(file.id),
    name: file.name,
    status: 'done',
    url: file.url,
    size: file.size,
  }));

  // 上传前校验
  const beforeUpload = (file: File) => {
    // 校验文件大小
    const isLtMaxSize = file.size / 1024 / 1024 < maxSize;
    if (!isLtMaxSize) {
      message.error(`文件大小不能超过 ${maxSize}MB`);
      return false;
    }

    // 校验文件类型
    if (accept) {
      const acceptTypes = accept.split(',').map((t) => t.trim());
      const fileType = file.type;
      const fileName = file.name.toLowerCase();
      const isValidType = acceptTypes.some((type) => {
        if (type.startsWith('.')) {
          return fileName.endsWith(type);
        }
        if (type.endsWith('/*')) {
          return fileType.startsWith(type.replace('/*', '/'));
        }
        return fileType === type;
      });

      if (!isValidType) {
        message.error('不支持的文件类型');
        return false;
      }
    }

    return true;
  };

  // 自定义上传
  const customRequest: UploadProps['customRequest'] = async (options) => {
    const { file, onSuccess, onError } = options;
    const formData = new FormData();
    formData.append('file', file as File);

    setUploading(true);
    setProgress(0);

    try {
      const response = await fetch('/api/files/upload', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${getAccessToken()}`,
        },
        body: formData,
      });

      const result = await response.json();

      if (result.code === 0) {
        const uploadedFile: UploadedFile = {
          id: result.data.id,
          url: result.data.url,
          name: result.data.name,
          size: result.data.size,
          type: result.data.type,
        };

        const newFiles = [...value, uploadedFile];
        onChange?.(newFiles);
        onSuccess?.(result.data);
        message.success('上传成功');
      } else {
        throw new Error(result.message || '上传失败');
      }
    } catch (error) {
      onError?.(error as Error);
      message.error((error as Error).message || '上传失败');
    } finally {
      setUploading(false);
      setProgress(0);
    }
  };

  // 删除文件
  const handleRemove = (file: UploadFile) => {
    const newFiles = value.filter((f) => String(f.id) !== file.uid);
    onChange?.(newFiles);
    return true;
  };

  const uploadButton =
    listType === 'picture-card' ? (
      <div>
        <PlusOutlined />
        <div style={{ marginTop: 8 }}>{buttonText}</div>
      </div>
    ) : (
      <Button icon={<UploadOutlined />} loading={uploading} disabled={disabled}>
        {buttonText}
      </Button>
    );

  return (
    <div>
      <Upload
        fileList={fileList}
        beforeUpload={beforeUpload}
        customRequest={customRequest}
        onRemove={handleRemove}
        maxCount={maxCount}
        accept={accept}
        disabled={disabled}
        listType={listType}
      >
        {fileList.length >= maxCount ? null : uploadButton}
      </Upload>
      {uploading && progress > 0 && (
        <Progress percent={progress} size="small" style={{ marginTop: 8 }} />
      )}
    </div>
  );
};

export default AttachmentUploader;
