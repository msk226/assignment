import React, { useState } from 'react';
import { Form, InputNumber, Button, Card, DatePicker, message, Descriptions, Spin } from 'antd';
import dayjs from 'dayjs';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { budgetApi } from '../api/endpoints';

const Budget: React.FC = () => {
  const [date, setDate] = useState(dayjs());
  const [form] = Form.useForm();
  const queryClient = useQueryClient();
  const [messageApi, contextHolder] = message.useMessage();

  // Fetch budget for selected date
  const { data: budgetData, isLoading } = useQuery({
    queryKey: ['budget', date.format('YYYY-MM-DD')],
    queryFn: () => budgetApi.getBudget(date.format('YYYY-MM-DD')),
  });

  // Update budget mutation
  const updateBudgetMutation = useMutation({
    mutationFn: budgetApi.updateBudget,
    onSuccess: () => {
      messageApi.success('예산이 성공적으로 업데이트되었습니다.');
      queryClient.invalidateQueries({ queryKey: ['budget'] });
      queryClient.invalidateQueries({ queryKey: ['dashboardStats'] });
    },
    onError: () => {
      messageApi.error('예산 업데이트 실패');
    }
  });

  const onFinish = (values: any) => {
    updateBudgetMutation.mutate({
      date: values.date.format('YYYY-MM-DD'),
      totalBudget: values.budget
    });
  };

  const handleDateChange = (newDate: dayjs.Dayjs | null) => {
    if (newDate) setDate(newDate);
  };

  return (
    <div>
      {contextHolder}
      <h2>예산 관리</h2>
      <Card title="일일 예산 설정" style={{ maxWidth: 600 }}>
        <Form
          form={form}
          layout="vertical"
          onFinish={onFinish}
          initialValues={{ date: dayjs(), budget: 100000 }}
        >
          <Form.Item name="date" label="날짜" rules={[{ required: true }]}>
            <DatePicker
              style={{ width: '100%' }}
              value={date}
              onChange={handleDateChange}
              allowClear={false}
            />
          </Form.Item>
          <Form.Item name="budget" label="총 예산 (Point)" rules={[{ required: true }]}>
            <InputNumber
              style={{ width: '100%' }}
              formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              parser={(value) => value?.replace(/\$\s?|(,*)/g, '') as unknown as number}
            />
          </Form.Item>
          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              size="large"
              loading={updateBudgetMutation.isPending}
              style={{ background: '#1A1A1A', borderColor: '#1A1A1A', color: '#C8FF00' }}
            >
              설정 저장
            </Button>
          </Form.Item>
        </Form>
      </Card>

      <Card title="현재 예산 상태" style={{ marginTop: 24, maxWidth: 600 }}>
        {isLoading ? <Spin /> : (
          <Descriptions column={1}>
            <Descriptions.Item label="선택된 날짜">{budgetData?.date || date.format('YYYY-MM-DD')}</Descriptions.Item>
            <Descriptions.Item label="총 예산">{budgetData?.totalBudget?.toLocaleString() ?? 0} P</Descriptions.Item>
            <Descriptions.Item label="사용된 예산">{budgetData?.usedBudget?.toLocaleString() ?? 0} P</Descriptions.Item>
            <Descriptions.Item label="잔여 예산">{budgetData?.remainingBudget?.toLocaleString() ?? 0} P</Descriptions.Item>
          </Descriptions>
        )}
      </Card>
    </div>
  );
};

export default Budget;
