import React from 'react';
import { Table, Tag, Button, Popconfirm, message, Spin } from 'antd';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { orderApi, Order } from '../api/endpoints';

const Orders: React.FC = () => {
  const queryClient = useQueryClient();
  const [messageApi, contextHolder] = message.useMessage();

  const { data: orders, isLoading } = useQuery({
    queryKey: ['orders'],
    queryFn: orderApi.getOrders,
  });

  const cancelMutation = useMutation({
    mutationFn: orderApi.cancelOrder,
    onSuccess: () => {
      messageApi.success('주문이 취소되었습니다.');
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
    onError: () => messageApi.error('주문 취소 실패')
  });

  const columns = [
    { title: '주문 ID', dataIndex: 'id', key: 'id' },
    { title: '사용자 ID', dataIndex: 'userId', key: 'userId' },
    { title: '상품 ID', dataIndex: 'productId', key: 'productId' },
    { title: '상품명', dataIndex: 'productName', key: 'productName' },
    { title: '사용 포인트', dataIndex: 'pointsUsed', key: 'pointsUsed', render: (val: number) => `${val.toLocaleString()} P` },
    {
      title: '상태',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        let color = status === 'COMPLETED' ? '#C8FF00' : '#ff4d4f';
        const style = status === 'COMPLETED' ? { color: 'black', fontWeight: 'bold' } : {};
        return (
          <Tag color={color} style={style}>
            {status}
          </Tag>
        );
      }
    },
    { title: '주문일시', dataIndex: 'createdAt', key: 'createdAt', render: (val: string) => new Date(val).toLocaleString() },
    {
      title: '관리',
      key: 'action',
      render: (_: any, record: Order) => (
        record.status === 'COMPLETED' ? (
          <Popconfirm title="주문을 취소하시겠습니까?" onConfirm={() => cancelMutation.mutate(record.id)}>
            <Button danger size="small" loading={cancelMutation.isPending && cancelMutation.variables === record.id}>취소/환불</Button>
          </Popconfirm>
        ) : <span style={{ color: '#ccc' }}>-</span>
      ),
    },
  ];

  return (
    <div>
      {contextHolder}
      <h2 style={{ marginBottom: 16 }}>주문 내역</h2>
      {isLoading ? <Spin /> : <Table dataSource={orders} columns={columns} rowKey="id" />}
    </div>
  );
};

export default Orders;
