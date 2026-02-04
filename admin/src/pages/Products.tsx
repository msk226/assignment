import React, { useState } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Space, Popconfirm, message, Spin } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { productApi, Product } from '../api/endpoints';

const Products: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [form] = Form.useForm();
  const queryClient = useQueryClient();
  const [messageApi, contextHolder] = message.useMessage();

  const { data: products, isLoading } = useQuery({
    queryKey: ['products'],
    queryFn: productApi.getProducts,
  });

  const createMutation = useMutation({
    mutationFn: productApi.createProduct,
    onSuccess: () => {
      messageApi.success('상품이 생성되었습니다.');
      queryClient.invalidateQueries({ queryKey: ['products'] });
      setIsModalOpen(false);
      form.resetFields();
    },
    onError: () => messageApi.error('생성 실패')
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number, data: Partial<Product> }) => productApi.updateProduct(id, data),
    onSuccess: () => {
      messageApi.success('상품이 수정되었습니다.');
      queryClient.invalidateQueries({ queryKey: ['products'] });
      setIsModalOpen(false);
      setEditingProduct(null);
      form.resetFields();
    },
    onError: () => messageApi.error('수정 실패')
  });

  const deleteMutation = useMutation({
    mutationFn: productApi.deleteProduct,
    onSuccess: () => {
      messageApi.success('상품이 삭제되었습니다.');
      queryClient.invalidateQueries({ queryKey: ['products'] });
    },
    onError: () => messageApi.error('삭제 실패')
  });

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id' },
    { title: '상품명', dataIndex: 'name', key: 'name' },
    {
      title: '가격',
      dataIndex: 'price',
      key: 'price',
      render: (val: number) => `${val.toLocaleString()} P`
    },
    { title: '재고', dataIndex: 'stock', key: 'stock' },
    { title: '설명', dataIndex: 'description', key: 'description', ellipsis: true },
    {
      title: '관리',
      key: 'action',
      render: (_: any, record: Product) => (
        <Space size="middle">
          <Button icon={<EditOutlined />} onClick={() => handleEdit(record)} />
          <Popconfirm title="삭제하시겠습니까?" onConfirm={() => deleteMutation.mutate(record.id)}>
            <Button icon={<DeleteOutlined />} danger loading={deleteMutation.isPending} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const handleEdit = (product: Product) => {
    setEditingProduct(product);
    form.setFieldsValue(product);
    setIsModalOpen(true);
  };

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      if (editingProduct) {
        updateMutation.mutate({ id: editingProduct.id, data: values });
      } else {
        createMutation.mutate(values);
      }
    } catch (error) {
      console.error('Validate Failed:', error);
    }
  };

  return (
    <div>
      {contextHolder}
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <h2>상품 관리</h2>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => { setEditingProduct(null); form.resetFields(); setIsModalOpen(true); }}
          style={{ background: '#1A1A1A', borderColor: '#1A1A1A', color: '#C8FF00' }}
        >
          상품 등록
        </Button>
      </div>
      {isLoading ? <Spin /> : <Table dataSource={products} columns={columns} rowKey="id" />}

      <Modal
        title={editingProduct ? "상품 수정" : "상품 등록"}
        open={isModalOpen}
        onOk={handleOk}
        onCancel={() => setIsModalOpen(false)}
        confirmLoading={createMutation.isPending || updateMutation.isPending}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="상품명" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="설명" rules={[{ required: true }]}>
            <Input.TextArea />
          </Form.Item>
          <Form.Item name="price" label="가격 (Point)" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="stock" label="재고" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="imageUrl" label="이미지 URL">
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Products;
