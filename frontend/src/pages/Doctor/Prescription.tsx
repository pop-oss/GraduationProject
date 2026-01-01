import { useState, useEffect } from 'react'
import { Card, Form, Input, Button, Table, InputNumber, Select, Space, message, Divider } from 'antd'
import { PlusOutlined, DeleteOutlined, SendOutlined } from '@ant-design/icons'
import { useParams, useNavigate } from 'react-router-dom'
import PageHeader from '@/components/PageHeader'
import { prescriptionService } from '@/services/prescription'
import { get } from '@/services/http'

const { TextArea } = Input

interface Drug {
  id: number
  name: string
  specification: string
  unit: string
}

interface PrescriptionItem {
  key: string
  drugId: number
  drugName: string
  specification: string
  dosage: string
  frequency: string
  duration: string
  quantity: number
  unit: string
  usage: string
}

/**
 * 开方页面
 * _Requirements: 6.8_
 */
const Prescription = () => {
  const { consultationId } = useParams<{ consultationId: string }>()
  const navigate = useNavigate()
  const [form] = Form.useForm()
  const [items, setItems] = useState<PrescriptionItem[]>([])
  const [drugs, setDrugs] = useState<Drug[]>([])
  const [loading, setLoading] = useState(false)
  const [selectedDrug, setSelectedDrug] = useState<Drug | null>(null)

  useEffect(() => {
    fetchDrugs()
  }, [])

  const fetchDrugs = async () => {
    try {
      const res = await get<Drug[]>('/drugs')
      setDrugs(res.data || mockDrugs)
    } catch {
      setDrugs(mockDrugs)
    }
  }

  const mockDrugs: Drug[] = [
    { id: 1, name: '阿莫西林胶囊', specification: '0.5g*24粒', unit: '盒' },
    { id: 2, name: '布洛芬缓释胶囊', specification: '0.3g*20粒', unit: '盒' },
    { id: 3, name: '氯雷他定片', specification: '10mg*6片', unit: '盒' },
    { id: 4, name: '盐酸氨溴索口服液', specification: '100ml', unit: '瓶' },
    { id: 5, name: '头孢克肟分散片', specification: '0.1g*10片', unit: '盒' },
    { id: 6, name: '氧氟沙星滴耳液', specification: '5ml', unit: '瓶' },
  ]

  const handleAddItem = () => {
    if (!selectedDrug) {
      message.warning('请先选择药品')
      return
    }
    
    const newItem: PrescriptionItem = {
      key: Date.now().toString(),
      drugId: selectedDrug.id,
      drugName: selectedDrug.name,
      specification: selectedDrug.specification,
      dosage: '',
      frequency: '',
      duration: '',
      quantity: 1,
      unit: selectedDrug.unit,
      usage: ''
    }
    
    setItems([...items, newItem])
    setSelectedDrug(null)
  }

  const handleRemoveItem = (key: string) => {
    setItems(items.filter(item => item.key !== key))
  }

  const handleItemChange = (key: string, field: string, value: unknown) => {
    setItems(items.map(item => 
      item.key === key ? { ...item, [field]: value } : item
    ))
  }

  const handleSubmit = async () => {
    if (items.length === 0) {
      message.warning('请添加至少一种药品')
      return
    }

    const values = await form.validateFields()
    setLoading(true)
    
    try {
      await prescriptionService.create({
        consultationId: consultationId!,
        diagnosis: values.diagnosis ? [values.diagnosis] : [],
        items: items.map(({ drugName, specification, dosage, frequency, quantity, unit, usage }) => ({
          drugName,
          spec: specification,
          usage: `${dosage} ${frequency} ${usage}`.trim(),
          quantity,
          unit,
          remark: '',
        })),
      })
      message.success('处方已提交审核')
      navigate(`/doctor/consultation/${consultationId}`)
    } catch (err) {
      message.error((err as Error).message || '提交失败')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    { title: '药品名称', dataIndex: 'drugName', key: 'drugName' },
    { title: '规格', dataIndex: 'specification', key: 'specification', width: 120 },
    { 
      title: '单次剂量', 
      dataIndex: 'dosage', 
      key: 'dosage',
      width: 100,
      render: (_: any, record: PrescriptionItem) => (
        <Input 
          size="small"
          value={record.dosage}
          onChange={(e) => handleItemChange(record.key, 'dosage', e.target.value)}
          placeholder="如: 1粒"
        />
      )
    },
    { 
      title: '用药频次', 
      dataIndex: 'frequency', 
      key: 'frequency',
      width: 120,
      render: (_: any, record: PrescriptionItem) => (
        <Select
          size="small"
          value={record.frequency}
          onChange={(v) => handleItemChange(record.key, 'frequency', v)}
          style={{ width: '100%' }}
          options={[
            { value: 'qd', label: '每日一次' },
            { value: 'bid', label: '每日两次' },
            { value: 'tid', label: '每日三次' },
            { value: 'qid', label: '每日四次' },
            { value: 'prn', label: '必要时' },
          ]}
        />
      )
    },
    { 
      title: '疗程', 
      dataIndex: 'duration', 
      key: 'duration',
      width: 80,
      render: (_: any, record: PrescriptionItem) => (
        <Input 
          size="small"
          value={record.duration}
          onChange={(e) => handleItemChange(record.key, 'duration', e.target.value)}
          placeholder="如: 7天"
        />
      )
    },
    { 
      title: '数量', 
      dataIndex: 'quantity', 
      key: 'quantity',
      width: 80,
      render: (_: any, record: PrescriptionItem) => (
        <InputNumber 
          size="small"
          min={1}
          value={record.quantity}
          onChange={(v) => handleItemChange(record.key, 'quantity', v || 1)}
        />
      )
    },
    { title: '单位', dataIndex: 'unit', key: 'unit', width: 60 },
    {
      title: '操作',
      key: 'action',
      width: 60,
      render: (_: any, record: PrescriptionItem) => (
        <Button 
          type="link" 
          danger 
          icon={<DeleteOutlined />}
          onClick={() => handleRemoveItem(record.key)}
        />
      )
    }
  ]

  return (
    <div style={{ padding: 24 }}>
      <PageHeader
        title="开具处方"
        breadcrumbs={[
          { title: '工作台', href: '/doctor' },
          { title: '问诊详情', href: `/doctor/consultation/${consultationId}` },
          { title: '开具处方' },
        ]}
        onBack={() => navigate(`/doctor/consultation/${consultationId}`)}
      />
      
      <Card>
        <Form form={form} layout="vertical">
          <Form.Item name="diagnosis" label="诊断" rules={[{ required: true }]}>
            <TextArea rows={2} placeholder="请输入诊断结果" />
          </Form.Item>
        </Form>

        <Divider>药品列表</Divider>
        
        <Space style={{ marginBottom: 16 }}>
          <Select
            showSearch
            style={{ width: 300 }}
            placeholder="搜索并选择药品"
            value={selectedDrug?.id}
            onChange={(id) => setSelectedDrug(drugs.find(d => d.id === id) || null)}
            filterOption={(input, option) =>
              (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
            }
            options={drugs.map(d => ({ value: d.id, label: `${d.name} (${d.specification})` }))}
          />
          <Button icon={<PlusOutlined />} onClick={handleAddItem}>
            添加药品
          </Button>
        </Space>

        <Table
          columns={columns}
          dataSource={items}
          rowKey="key"
          pagination={false}
          size="small"
        />

        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="notes" label="医嘱备注">
            <TextArea rows={2} placeholder="其他注意事项..." />
          </Form.Item>
        </Form>

        <div style={{ marginTop: 24, textAlign: 'right' }}>
          <Button onClick={() => navigate(`/doctor/consultation/${consultationId}`)} style={{ marginRight: 8 }}>
            取消
          </Button>
          <Button type="primary" icon={<SendOutlined />} onClick={handleSubmit} loading={loading}>
            提交审核
          </Button>
        </div>
      </Card>
    </div>
  )
}

export default Prescription
