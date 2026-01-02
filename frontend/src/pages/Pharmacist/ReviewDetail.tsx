import { useState, useEffect } from 'react'
import { Card, Descriptions, Table, Button, Input, Tag, Alert, Space, Divider, message, Spin } from 'antd'
import { CheckCircleOutlined, CloseCircleOutlined, WarningOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import { useParams, useNavigate, useSearchParams } from 'react-router-dom'
import type { ColumnsType } from 'antd/es/table'
import PageHeader from '@/components/PageHeader'
import PatientBriefCard from '@/components/PatientBriefCard'
import { reviewService, type ReviewDetail as ReviewDetailType, type PrescriptionItem, type RiskLevel } from '@/services/review'
import { formatDateTime } from '@/utils/date'

const { TextArea } = Input

const riskColors: Record<RiskLevel, string> = {
  LOW: 'green',
  MEDIUM: 'orange',
  HIGH: 'red',
}

const riskLabels: Record<RiskLevel, string> = {
  LOW: '低风险',
  MEDIUM: '中风险',
  HIGH: '高风险',
}

/**
 * 审方详情页面
 * _Requirements: 8.2, 8.3, 8.4_
 */
const ReviewDetail = () => {
  const { id } = useParams<{ id: string }>()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const [prescription, setPrescription] = useState<ReviewDetailType | null>(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [rejectReason, setRejectReason] = useState('')
  const [showReject, setShowReject] = useState(searchParams.get('action') === 'reject')

  useEffect(() => { 
    if (id) fetchDetail() 
  }, [id])

  const fetchDetail = async () => {
    setLoading(true)
    try {
      const data = await reviewService.getDetail(id!)
      setPrescription(data)
    } catch (err) { 
      message.error((err as Error).message || '获取处方详情失败') 
    } finally { 
      setLoading(false) 
    }
  }

  const handleApprove = async () => {
    setSubmitting(true)
    try {
      await reviewService.approve(id!)
      message.success('处方已通过审核')
      navigate('/pharmacist')
    } catch (err) { 
      message.error((err as Error).message || '操作失败') 
    } finally { 
      setSubmitting(false) 
    }
  }

  const handleReject = async () => {
    if (!rejectReason.trim()) { 
      message.warning('请填写驳回原因')
      return 
    }
    setSubmitting(true)
    try {
      await reviewService.reject(id!, rejectReason)
      message.success('处方已驳回')
      navigate('/pharmacist')
    } catch (err) { 
      message.error((err as Error).message || '操作失败') 
    } finally { 
      setSubmitting(false) 
    }
  }

  const columns: ColumnsType<PrescriptionItem> = [
    { title: '药品名称', dataIndex: 'drugName', key: 'drugName' },
    { title: '规格', dataIndex: 'drugSpec', key: 'drugSpec' },
    { title: '单次剂量', dataIndex: 'dosage', key: 'dosage' },
    { title: '用药频次', dataIndex: 'frequency', key: 'frequency' },
    { title: '疗程', dataIndex: 'duration', key: 'duration' },
    { title: '数量', dataIndex: 'quantity', key: 'quantity', align: 'center' },
    { title: '单位', dataIndex: 'unit', key: 'unit' },
  ]

  if (loading) {
    return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />
  }

  if (!prescription) {
    return (
      <div style={{ padding: 24 }}>
        <Card>
          <div style={{ textAlign: 'center', padding: 40 }}>
            处方不存在或已被删除
            <br />
            <Button type="link" onClick={() => navigate('/pharmacist')}>返回列表</Button>
          </div>
        </Card>
      </div>
    )
  }

  const isPending = prescription.status === 'PENDING_REVIEW'

  return (
    <div style={{ padding: 24 }}>
      <PageHeader
        title="处方审核"
        breadcrumbs={[
          { title: '药师工作台', href: '/pharmacist' },
          { title: '待审处方', href: '/pharmacist' },
          { title: '审核详情' }
        ]}
        extra={
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/pharmacist')}>
            返回列表
          </Button>
        }
      />

      <Card>
        {/* 风险提示 */}
        {prescription.riskWarnings && prescription.riskWarnings.length > 0 && (
          <Alert 
            type="warning" 
            icon={<WarningOutlined />} 
            showIcon 
            style={{ marginBottom: 16 }}
            message="风险提示" 
            description={
              <ul style={{ margin: 0, paddingLeft: 20 }}>
                {prescription.riskWarnings.map((w, i) => <li key={i}>{w}</li>)}
              </ul>
            } 
          />
        )}

        {/* 患者信息 */}
        <div style={{ marginBottom: 16 }}>
          <h4>患者信息</h4>
          <PatientBriefCard
            patient={{
              id: prescription.patientId,
              name: prescription.patientName,
              age: prescription.patientAge,
              gender: prescription.patientGender,
            }}
          />
          {prescription.allergies && prescription.allergies.length > 0 && (
            <Alert
              type="error"
              message={`过敏史: ${prescription.allergies.join(', ')}`}
              style={{ marginTop: 8 }}
            />
          )}
        </div>

        <Divider />

        {/* 处方信息 */}
        <Descriptions bordered column={2}>
          <Descriptions.Item label="处方编号">{prescription.prescriptionNo}</Descriptions.Item>
          <Descriptions.Item label="开方医生">{prescription.doctorName}</Descriptions.Item>
          <Descriptions.Item label="诊断" span={2}>{prescription.diagnosis}</Descriptions.Item>
          <Descriptions.Item label="开方时间">{formatDateTime(prescription.createdAt)}</Descriptions.Item>
          <Descriptions.Item label="风险等级">
            <Tag color={riskColors[prescription.riskLevel]}>
              {riskLabels[prescription.riskLevel] || prescription.riskLevel}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="状态">
            <Tag color={isPending ? 'processing' : prescription.status === 'APPROVED' ? 'success' : 'error'}>
              {isPending ? '待审核' : prescription.status === 'APPROVED' ? '已通过' : '已驳回'}
            </Tag>
          </Descriptions.Item>
          {prescription.reviewedAt && (
            <Descriptions.Item label="审核时间">{formatDateTime(prescription.reviewedAt)}</Descriptions.Item>
          )}
          {prescription.rejectReason && (
            <Descriptions.Item label="驳回原因" span={2}>
              <span style={{ color: '#ff4d4f' }}>{prescription.rejectReason}</span>
            </Descriptions.Item>
          )}
        </Descriptions>

        <Divider>药品明细</Divider>

        <Table 
          columns={columns} 
          dataSource={prescription.items} 
          rowKey="id" 
          pagination={false}
          size="small"
        />
        
        {/* 审核操作 */}
        {isPending && (
          <>
            <Divider />
            {showReject ? (
              <Space direction="vertical" style={{ width: '100%' }}>
                <TextArea 
                  rows={3} 
                  placeholder="请填写驳回原因（必填）..." 
                  value={rejectReason} 
                  onChange={e => setRejectReason(e.target.value)}
                  maxLength={500}
                  showCount
                />
                <Space>
                  <Button onClick={() => setShowReject(false)}>取消</Button>
                  <Button danger onClick={handleReject} loading={submitting}>确认驳回</Button>
                </Space>
              </Space>
            ) : (
              <Space>
                <Button 
                  type="primary" 
                  icon={<CheckCircleOutlined />} 
                  onClick={handleApprove} 
                  loading={submitting}
                  size="large"
                >
                  通过审核
                </Button>
                <Button 
                  danger 
                  icon={<CloseCircleOutlined />} 
                  onClick={() => setShowReject(true)}
                  size="large"
                >
                  驳回处方
                </Button>
              </Space>
            )}
          </>
        )}
      </Card>
    </div>
  )
}

export default ReviewDetail
