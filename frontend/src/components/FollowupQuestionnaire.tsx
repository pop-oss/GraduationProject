/**
 * 随访问卷组件
 * _Requirements: 5.4, 5.5_
 */

import React from 'react';
import { Form, Input, Radio, Checkbox, InputNumber, Rate, Button, Space, Card } from 'antd';
import type { FollowupQuestion, FollowupAnswer } from '@/types';

export interface FollowupQuestionnaireProps {
  /** 问题列表 */
  questions: FollowupQuestion[];
  /** 初始答案 */
  initialAnswers?: FollowupAnswer[];
  /** 提交回调 */
  onSubmit: (answers: FollowupAnswer[]) => Promise<void>;
  /** 是否只读 */
  readonly?: boolean;
  /** 是否加载中 */
  loading?: boolean;
}

/**
 * 根据问题类型渲染输入组件
 */
const renderQuestionInput = (question: FollowupQuestion, readonly: boolean) => {
  const { type, options, placeholder, min, max } = question;

  switch (type) {
    case 'text':
      return (
        <Input.TextArea
          placeholder={placeholder || '请输入'}
          rows={3}
          disabled={readonly}
        />
      );

    case 'single':
      return (
        <Radio.Group disabled={readonly}>
          <Space direction="vertical">
            {options?.map((opt) => (
              <Radio key={opt.value} value={opt.value}>
                {opt.label}
              </Radio>
            ))}
          </Space>
        </Radio.Group>
      );

    case 'multi':
      return (
        <Checkbox.Group disabled={readonly}>
          <Space direction="vertical">
            {options?.map((opt) => (
              <Checkbox key={opt.value} value={opt.value}>
                {opt.label}
              </Checkbox>
            ))}
          </Space>
        </Checkbox.Group>
      );

    case 'number':
      return (
        <InputNumber
          placeholder={placeholder || '请输入数字'}
          min={min}
          max={max}
          disabled={readonly}
          style={{ width: 200 }}
        />
      );

    case 'rate':
      return <Rate disabled={readonly} count={max || 5} />;

    default:
      return <Input placeholder={placeholder || '请输入'} disabled={readonly} />;
  }
};

/**
 * 随访问卷组件
 * 支持多种问题类型渲染和必填校验
 * _Requirements: 5.4, 5.5_
 */
export const FollowupQuestionnaire: React.FC<FollowupQuestionnaireProps> = ({
  questions,
  initialAnswers = [],
  onSubmit,
  readonly = false,
  loading = false,
}) => {
  const [form] = Form.useForm();

  // 转换初始答案为表单值
  const initialValues = initialAnswers.reduce(
    (acc, answer) => {
      acc[`question_${answer.questionId}`] = answer.value;
      return acc;
    },
    {} as Record<string, unknown>
  );

  const handleSubmit = async (values: Record<string, unknown>) => {
    const answers: FollowupAnswer[] = questions.map((q) => ({
      questionId: q.id,
      value: values[`question_${q.id}`],
    }));
    await onSubmit(answers);
  };

  return (
    <Form
      form={form}
      layout="vertical"
      initialValues={initialValues}
      onFinish={handleSubmit}
    >
      {questions.map((question, index) => (
        <Card
          key={question.id}
          size="small"
          style={{ marginBottom: 16 }}
          title={
            <span>
              {index + 1}. {question.title}
              {question.required && <span style={{ color: '#ff4d4f', marginLeft: 4 }}>*</span>}
            </span>
          }
        >
          <Form.Item
            name={`question_${question.id}`}
            rules={[
              {
                required: question.required,
                message: '此题为必答题',
              },
            ]}
            style={{ marginBottom: 0 }}
          >
            {renderQuestionInput(question, readonly)}
          </Form.Item>
        </Card>
      ))}

      {!readonly && (
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={loading}>
              提交问卷
            </Button>
            <Button onClick={() => form.resetFields()}>重置</Button>
          </Space>
        </Form.Item>
      )}
    </Form>
  );
};

export default FollowupQuestionnaire;
