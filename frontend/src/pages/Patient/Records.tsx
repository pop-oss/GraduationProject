import { useState, useEffect } from 'react'
import { Card, Table, Button, Modal, Descriptions, List, message } from 'antd'
import { FileTextOutlined, PaperClipOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import api from '@/services/api'

interface MedicalRecord {
  id: number
  consultationId: number
  doctorName: string
  chiefComplaint: string
  presentIllness: string
  diagnosis: string
  treatment: string
  createdAt: string
  attachments?: { id: number; fileName: string; fileUrl: string }[]
}

/**
 * 病历查看页面
 * _Requirements: 5.5_
 */
const Records = () => {
  const [records, setRecords] = useState<MedicalRecord[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedRecord, setSelectedRecord] = useState<MedicalRecord | null>(null)
  const [modalVisible, setModalVisible] = useState(false)

  useEffect(() => {
    fetchRecords()
  }, [])

  const fetchRecords = async () => {
    try {
      const res = await api.get('/medical-records/my')
      // 兼容分页响应格式，确保返回数组
      let recordList: MedicalRecord[] = []
      if (res?.data?.data) {
        if (Array.isArray(res.data.data.records)) {
          recordList = res.data.data.records
        } else if (Array.isArray(res.data.data)) {
          recordList = res.data.data
        }
      }
      setRecords(recordList)
    } catch {
      message.error('获取病历列表失败')
      setRecords([])
    } finally {
      setLoading(false)
    }
  }

  const handleView = (record: MedicalRecord) => {
    setSelectedRecord(record)
    setModalVisible(true)
  }

  const columns: ColumnsType<MedicalRecord> = [
    { title: '就诊日期', dataIndex: 'createdAt', key: 'createdAt', 
      render: (text) => new Date(text).toLocaleDateString() },
    { title: '接诊医生', dataIndex: 'doctorName', key: 'doctorName' },
    { title: '主诉', dataIndex: 'chiefComplaint', key: 'chiefComplaint', ellipsis: true },
    { title: '诊断', dataIndex: 'diagnosis', key: 'diagnosis', ellipsis: true },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Button type="link" icon={<FileTextOutlined />} onClick={() => handleView(record)}>
          查看详情
        </Button>
      ),
    },
  ]

  return (
    <div style={{ padding: 24 }}>
      <Card title="我的病历">
        <Table
          columns={columns}
          dataSource={records}
          rowKey="id"
          loading={loading}
        />
      </Card>

      <Modal
        title="病历详情"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={700}
      >
        {selectedRecord && (
          <>
            <Descriptions bordered column={1}>
              <Descriptions.Item label="就诊日期">
                {new Date(selectedRecord.createdAt).toLocaleString()}
              </Descriptions.Item>
              <Descriptions.Item label="接诊医生">{selectedRecord.doctorName}</Descriptions.Item>
              <Descriptions.Item label="主诉">{selectedRecord.chiefComplaint}</Descriptions.Item>
              <Descriptions.Item label="现病史">{selectedRecord.presentIllness}</Descriptions.Item>
              <Descriptions.Item label="诊断">{selectedRecord.diagnosis}</Descriptions.Item>
              <Descriptions.Item label="治疗方案">{selectedRecord.treatment}</Descriptions.Item>
            </Descriptions>
            {selectedRecord.attachments && selectedRecord.attachments.length > 0 && (
              <Card title="附件" size="small" style={{ marginTop: 16 }}>
                <List
                  dataSource={selectedRecord.attachments}
                  renderItem={(item) => (
                    <List.Item>
                      <a href={item.fileUrl} target="_blank" rel="noopener noreferrer">
                        <PaperClipOutlined /> {item.fileName}
                      </a>
                    </List.Item>
                  )}
                />
              </Card>
            )}
          </>
        )}
      </Modal>
    </div>
  )
}

export default Records
