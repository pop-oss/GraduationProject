/**
 * 确认按钮组件
 * _Requirements: 2.4_
 */

import React, { useState } from 'react';
import { Button, Popconfirm, ButtonProps } from 'antd';

export interface ConfirmButtonProps {
  /** 确认弹窗标题 */
  confirmTitle: string;
  /** 确认弹窗描述 */
  confirmDescription?: string;
  /** 确认按钮文字 */
  okText?: string;
  /** 取消按钮文字 */
  cancelText?: string;
  /** 确认回调（支持异步） */
  onConfirm: () => Promise<void> | void;
  /** 按钮属性 */
  buttonProps?: ButtonProps;
  /** 按钮内容 */
  children: React.ReactNode;
  /** 是否禁用 */
  disabled?: boolean;
}

/**
 * 确认按钮组件
 * 实现二次确认弹窗和 loading 状态防重复提交
 * _Requirements: 2.4_
 */
export const ConfirmButton: React.FC<ConfirmButtonProps> = ({
  confirmTitle,
  confirmDescription,
  okText = '确定',
  cancelText = '取消',
  onConfirm,
  buttonProps,
  children,
  disabled,
}) => {
  const [loading, setLoading] = useState(false);

  const handleConfirm = async () => {
    // 防止重复提交
    if (loading) return;

    setLoading(true);
    try {
      await onConfirm();
    } finally {
      setLoading(false);
    }
  };

  return (
    <Popconfirm
      title={confirmTitle}
      description={confirmDescription}
      okText={okText}
      cancelText={cancelText}
      onConfirm={handleConfirm}
      okButtonProps={{ loading }}
      disabled={disabled || loading}
    >
      <Button {...buttonProps} loading={loading} disabled={disabled || loading}>
        {children}
      </Button>
    </Popconfirm>
  );
};

export default ConfirmButton;
