/**
 * 数据表格组件
 * _Requirements: 2.6_
 */

import { Table, TableProps } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import EmptyState from './EmptyState';

export interface DataTableProps<T> {
  /** 表格列配置 */
  columns: ColumnsType<T>;
  /** 数据源 */
  dataSource: T[];
  /** 行键 */
  rowKey: string | ((row: T) => string);
  /** 是否加载中 */
  loading: boolean;
  /** 当前页码 */
  page: number;
  /** 每页条数 */
  pageSize: number;
  /** 总条数 */
  total: number;
  /** 分页变化回调 */
  onPageChange: (page: number, pageSize: number) => void;
  /** 空状态文字 */
  emptyText?: string;
  /** 空状态描述 */
  emptyDescription?: string;
  /** 空状态操作按钮文字 */
  emptyActionText?: string;
  /** 空状态操作回调 */
  onEmptyAction?: () => void;
  /** 表格其他属性 */
  tableProps?: Omit<TableProps<T>, 'columns' | 'dataSource' | 'rowKey' | 'loading' | 'pagination'>;
}

/**
 * 数据表格组件
 * 封装分页、加载、空态
 * _Requirements: 2.6_
 */
export function DataTable<T extends object>({
  columns,
  dataSource,
  rowKey,
  loading,
  page,
  pageSize,
  total,
  onPageChange,
  emptyText = '暂无数据',
  emptyDescription,
  emptyActionText,
  onEmptyAction,
  tableProps,
}: DataTableProps<T>) {
  return (
    <Table<T>
      columns={columns}
      dataSource={dataSource}
      rowKey={rowKey}
      loading={loading}
      pagination={{
        current: page,
        pageSize: pageSize,
        total: total,
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (total) => `共 ${total} 条`,
        onChange: onPageChange,
        onShowSizeChange: onPageChange,
      }}
      locale={{
        emptyText: (
          <EmptyState
            title={emptyText}
            description={emptyDescription}
            actionText={emptyActionText}
            onAction={onEmptyAction}
          />
        ),
      }}
      {...tableProps}
    />
  );
}

export default DataTable;
