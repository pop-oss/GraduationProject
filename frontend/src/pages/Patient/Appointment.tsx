import { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  List,
  Avatar,
  Tag,
  Button,
  DatePicker,
  Modal,
  Form,
  Input,
  message,
  Select,
  Spin,
} from 'antd';
import { UserOutlined, ClockCircleOutlined } from '@ant-design/icons';
import dayjs, { Dayjs } from 'dayjs';
import { useNavigate } from 'react-router-dom';
import { get } from '@/services/http';
import { consultationService } from '@/services/consultation';
import AttachmentUploader from '@/components/AttachmentUploader';
import PageHeader from '@/components/PageHeader';
import type { UploadedFile, Id } from '@/types';

const { TextArea } = Input;

interface Doctor {
  id: Id;
  realName: string;
  title: string;
  departmentName: string;
  specialty: string;
  avatar?: string;
}

interface Department {
  id: Id;
  name: string;
}

interface TimeSlot {
  id: number;
  startTime: string;
  endTime: string;
  available: boolean;
}

/**
 * 预约问诊页面
 * _Requirements: 4.1, 4.2_
 */
const Appointment = () => {
  const navigate = useNavigate();
  const [departments, setDepartments] = useState<Department[]>([]);
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [selectedDepartment, setSelectedDepartment] = useState<Id | null>(null);
  const [selectedDoctor, setSelectedDoctor] = useState<Doctor | null>(null);
  const [selectedDate, setSelectedDate] = useState<Dayjs>(dayjs());
  const [timeSlots, setTimeSlots] = useState<TimeSlot[]>([]);
  const [selectedSlot, setSelectedSlot] = useState<TimeSlot | null>(null);
  const [modalVisible, setModalVisible] = useState(false);
  const [loading, setLoading] = useState(false);
  const [doctorsLoading, setDoctorsLoading] = useState(false);
  const [attachments, setAttachments] = useState<UploadedFile[]>([]);
  const [form] = Form.useForm();

  // 加载科室列表
  useEffect(() => {
    const fetchDepartments = async () => {
      try {
        const res = await get<any>('/departments', undefined, { skipErrorHandler: true });
        // 兼容分页响应格式，确保返回数组
        let deptList: Department[] = [];
        if (res?.data) {
          if (Array.isArray(res.data.records)) {
            deptList = res.data.records;
          } else if (Array.isArray(res.data)) {
            deptList = res.data;
          }
        }
        setDepartments(deptList.length > 0 ? deptList : [
          { id: 1, name: '耳科' },
          { id: 2, name: '鼻科' },
          { id: 3, name: '咽喉科' },
        ]);
      } catch {
        // 使用模拟数据
        setDepartments([
          { id: 1, name: '耳科' },
          { id: 2, name: '鼻科' },
          { id: 3, name: '咽喉科' },
        ]);
      }
    };
    fetchDepartments();
  }, []);

  // 根据科室加载医生列表
  useEffect(() => {
    const fetchDoctors = async () => {
      setDoctorsLoading(true);
      try {
        const params = selectedDepartment 
          ? { departmentId: selectedDepartment, current: 1, size: 100 } 
          : { current: 1, size: 100 };
        const res = await get<any>('/doctors', params, { skipErrorHandler: true });
        // 兼容分页响应格式，确保返回数组
        let doctorList: Doctor[] = [];
        if (res?.data) {
          if (Array.isArray(res.data.records)) {
            doctorList = res.data.records;
          } else if (Array.isArray(res.data)) {
            doctorList = res.data;
          }
        }
        setDoctors(doctorList);
      } catch {
        setDoctors([]);
      } finally {
        setDoctorsLoading(false);
      }
    };
    fetchDoctors();
  }, [selectedDepartment]);

  // 加载时间段
  useEffect(() => {
    if (selectedDoctor && selectedDate) {
      fetchTimeSlots();
    }
  }, [selectedDoctor, selectedDate]);

  const fetchTimeSlots = async () => {
    if (!selectedDoctor) return;
    try {
      const res = await get<TimeSlot[]>(`/doctors/${selectedDoctor.id}/schedule`, {
        date: selectedDate.format('YYYY-MM-DD'),
      }, { skipErrorHandler: true });
      // 确保返回数组
      let slots: TimeSlot[] = [];
      if (res?.data) {
        if (Array.isArray(res.data)) {
          slots = res.data;
        }
      }
      setTimeSlots(slots.length > 0 ? slots : generateMockSlots());
    } catch {
      // 静默处理错误，使用模拟数据
      setTimeSlots(generateMockSlots());
    }
  };

  const generateMockSlots = (): TimeSlot[] => {
    const slots: TimeSlot[] = [];
    for (let h = 9; h < 17; h++) {
      slots.push({
        id: h,
        startTime: `${h}:00`,
        endTime: `${h}:30`,
        available: Math.random() > 0.3,
      });
      slots.push({
        id: h + 100,
        startTime: `${h}:30`,
        endTime: `${h + 1}:00`,
        available: Math.random() > 0.3,
      });
    }
    return slots;
  };

  // 提交预约
  // _Requirements: 4.2_
  const handleSubmit = async (values: { symptoms: string }) => {
    if (!selectedDoctor || !selectedSlot) return;
    setLoading(true);
    try {
      const scheduledAt = `${selectedDate.format('YYYY-MM-DD')} ${selectedSlot.startTime}:00`;
      await consultationService.create({
        doctorId: selectedDoctor.id,
        symptoms: values.symptoms,
        attachmentIds: attachments.map((a) => a.id),
        scheduledAt,
      });
      message.success('预约成功');
      setModalVisible(false);
      form.resetFields();
      setSelectedSlot(null);
      setAttachments([]);
      navigate('/patient/consultations');
    } catch (err) {
      message.error((err as Error).message || '预约失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <PageHeader
        title="预约问诊"
        subtitle="选择医生和时间，预约在线问诊"
        breadcrumbs={[{ title: '首页', href: '/patient' }, { title: '预约问诊' }]}
      />

      <Row gutter={24}>
        {/* 科室和医生选择 */}
        <Col xs={24} md={8}>
          <Card title="选择科室和医生">
            <Select
              placeholder="选择科室（可选）"
              allowClear
              style={{ width: '100%', marginBottom: 16 }}
              onChange={(value) => setSelectedDepartment(value)}
              options={departments.map((d) => ({ value: d.id, label: d.name }))}
            />

            {doctorsLoading ? (
              <div style={{ textAlign: 'center', padding: 24 }}>
                <Spin />
              </div>
            ) : (
              <List
                dataSource={doctors}
                renderItem={(doctor) => (
                  <List.Item
                    onClick={() => setSelectedDoctor(doctor)}
                    style={{
                      cursor: 'pointer',
                      background: selectedDoctor?.id === doctor.id ? '#e6f7ff' : 'transparent',
                      borderRadius: 4,
                      padding: 8,
                    }}
                  >
                    <List.Item.Meta
                      avatar={<Avatar icon={<UserOutlined />} src={doctor.avatar} />}
                      title={
                        <>
                          {doctor.realName} <Tag>{doctor.title}</Tag>
                        </>
                      }
                      description={`${doctor.departmentName} | ${doctor.specialty}`}
                    />
                  </List.Item>
                )}
              />
            )}
          </Card>
        </Col>

        {/* 时间选择 */}
        <Col xs={24} md={16}>
          <Card title="选择时间">
            <DatePicker
              value={selectedDate}
              onChange={(date) => date && setSelectedDate(date)}
              disabledDate={(current) => current && current < dayjs().startOf('day')}
              style={{ marginBottom: 16 }}
            />
            <Row gutter={[8, 8]}>
              {timeSlots.map((slot) => (
                <Col span={6} key={slot.id}>
                  <Button
                    block
                    type={selectedSlot?.id === slot.id ? 'primary' : 'default'}
                    disabled={!slot.available}
                    onClick={() => setSelectedSlot(slot)}
                    icon={<ClockCircleOutlined />}
                  >
                    {slot.startTime}
                  </Button>
                </Col>
              ))}
            </Row>
            {selectedDoctor && selectedSlot && (
              <Button type="primary" style={{ marginTop: 24 }} onClick={() => setModalVisible(true)}>
                确认预约
              </Button>
            )}
          </Card>
        </Col>
      </Row>

      {/* 预约确认弹窗 */}
      <Modal
        title="确认预约信息"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={600}
      >
        <p>
          <strong>医生：</strong>
          {selectedDoctor?.realName} ({selectedDoctor?.title})
        </p>
        <p>
          <strong>时间：</strong>
          {selectedDate.format('YYYY-MM-DD')} {selectedSlot?.startTime}
        </p>

        <Form form={form} onFinish={handleSubmit} layout="vertical">
          <Form.Item
            name="symptoms"
            label="症状描述"
            rules={[{ required: true, message: '请描述您的症状' }]}
          >
            <TextArea rows={4} placeholder="请详细描述您的症状，以便医生更好地了解您的情况..." />
          </Form.Item>

          <Form.Item label="上传附件（可选）">
            <AttachmentUploader
              value={attachments}
              onChange={setAttachments}
              maxCount={5}
              maxSize={10}
              accept="image/*,.pdf"
            />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              提交预约
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Appointment;
