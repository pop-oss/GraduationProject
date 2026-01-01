/**
 * 病历编辑页面
 * _Requirements: 6.7_
 */

import { useState, useEffect } from 'react';
import { Card, Form, Input, Button, Space, message, Spin } from 'antd';
import { SaveOutlined } from '@ant-design/icons';
import { useParams, useNavigate } from 'react-router-dom';
import PageHeader from '@/components/PageHeader';
import { medicalRecordService, type MedicalRecord } from '@/services/medicalRecord';
import { consultationService } from '@/services/consultation';
import type { ConsultationDetail } from '@/types';

const { TextArea } = Input;

/**
 * 病历编辑页面
 * _Requirements: 6.7_
 */
const MedicalRecordPage = () => {
  const { consultationId } = useParams<{ consultationId: string }>();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [consultation, setConsultation] = useState<ConsultationDetail | null>(null);
  const [existingRecord, setExistingRecord] = useState<MedicalRecord | null>(null);

  useEffect(() => {
    loadData();
  }, [consultationId]);

  const loadData = async () => {
    if (!consultationId) return;
    setLoading(true);
    try {
      const [consultData, recordData] = await Promise.all([
        consultationService.getDetail(consultationId),
        medicalRecordService.getByConsultation(consultationId),
      ]);
      setConsultation(consultData);
      setExistingRecord(recordData);
      if (recordData) {
        form.setFieldsValue(recordData);
      }
    } catch (err) {
      message.error((err as Error).message || '加载失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async (values: Record<string, unknown>) => {
    if (!consultationId) return;
    setSaving(true);
    try {
      await medicalRecordService.save({
        consultationId,
        ...values,
      });
      message.success('病历保存成功');
      navigate(`/doctor/consultation/${consultationId}`);
    } catch (err) {
      message.error((err as Error).message || '保存失败');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div>
      <PageHeader
        title={existingRecord ? '编辑病历' : '填写病历'}
        breadcrumbs={[
          { title: '工作台', href: '/doctor' },
          { title: '问诊详情', href: `/doctor/consultation/${consultationId}` },
          { title: existingRecord ? '编辑病历' : '填写病历' },
        ]}
        onBack={() => navigate(`/doctor/consultation/${consultationId}`)}
      />

      {/* 患者信息摘要 */}
      {consultation?.patient && (
        <Card size="small" style={{ marginBottom: 24 }}>
          <Space size="large">
            <span>患者：{consultation.patient.name}</span>
            <span>性别：{consultation.patient.gender === 'MALE' ? '男' : consultation.patient.gender === 'FEMALE' ? '女' : '-'}</span>
            <span>年龄：{consultation.patient.age ? `${consultation.patient.age}岁` : '-'}</span>
          </Space>
        </Card>
      )}

      <Card>
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSave}
          style={{ maxWidth: 800 }}
        >
          <Form.Item
            name="chiefComplaint"
            label="主诉"
            rules={[{ required: true, message: '请填写主诉' }]}
          >
            <TextArea rows={2} placeholder="患者主要症状和持续时间，如：耳痛3天" />
          </Form.Item>

          <Form.Item
            name="presentIllness"
            label="现病史"
            rules={[{ required: true, message: '请填写现病史' }]}
          >
            <TextArea rows={4} placeholder="疾病发生、发展、演变过程，包括起病时间、诱因、主要症状及其变化等" />
          </Form.Item>

          <Form.Item name="pastHistory" label="既往史">
            <TextArea rows={2} placeholder="既往疾病史、手术史、外伤史、输血史、过敏史等" />
          </Form.Item>

          <Form.Item name="allergies" label="过敏史">
            <TextArea rows={2} placeholder="药物过敏、食物过敏等" />
          </Form.Item>

          <Form.Item name="physicalExam" label="体格检查">
            <TextArea rows={3} placeholder="专科检查结果，如：外耳道充血、鼓膜完整等" />
          </Form.Item>

          <Form.Item
            name="diagnosis"
            label="诊断"
            rules={[{ required: true, message: '请填写诊断' }]}
          >
            <TextArea rows={2} placeholder="诊断结论，如：1. 急性中耳炎 2. 外耳道炎" />
          </Form.Item>

          <Form.Item
            name="treatment"
            label="治疗方案"
            rules={[{ required: true, message: '请填写治疗方案' }]}
          >
            <TextArea rows={3} placeholder="治疗建议和方案" />
          </Form.Item>

          <Form.Item name="advice" label="医嘱">
            <TextArea rows={2} placeholder="注意事项、复诊建议等" />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<SaveOutlined />} loading={saving}>
                保存病历
              </Button>
              <Button onClick={() => navigate(`/doctor/consultation/${consultationId}`)}>
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default MedicalRecordPage;
