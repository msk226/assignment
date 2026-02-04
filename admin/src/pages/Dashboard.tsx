import React from 'react';
import { Card, Col, Row, Statistic, Spin, Alert } from 'antd';
import { UserOutlined, RiseOutlined, DollarOutlined, GiftOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { dashboardApi } from '../api/endpoints';

const Dashboard: React.FC = () => {
  const { data: stats, isLoading, error } = useQuery({
    queryKey: ['dashboardStats'],
    queryFn: dashboardApi.getStats,
    refetchInterval: 30000, // Refresh every 30s
  });

  if (isLoading) {
    return <div style={{ textAlign: 'center', marginTop: 50 }}><Spin size="large" /></div>;
  }

  if (error) {
    return <Alert message="Error" description="대시보드 데이터를 불러오는데 실패했습니다." type="error" showIcon />;
  }

  return (
    <div>
      <h2 style={{ marginBottom: '20px' }}>대시보드 ({stats?.date})</h2>
      <Row gutter={16}>
        <Col span={6}>
          <Card bordered={false}>
            <Statistic
              title="오늘 예산"
              value={stats?.totalBudget}
              prefix={<DollarOutlined />}
              suffix="P"
              valueStyle={{ color: '#3f8600' }}
            />
            <div style={{ marginTop: 8, fontSize: 12, color: '#888' }}>
              사용됨: {stats?.usedBudget.toLocaleString()} P ({Math.round(((stats?.usedBudget || 0) / (stats?.totalBudget || 1)) * 100)}%)
            </div>
          </Card>
        </Col>
        <Col span={6}>
          <Card bordered={false}>
            <Statistic
              title="오늘 참여자"
              value={stats?.participantCount}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card bordered={false}>
            <Statistic
              title="지급된 총 포인트"
              value={stats?.totalPointsDistributed}
              prefix={<GiftOutlined />}
              suffix="P"
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card bordered={false}>
            <Statistic
              title="잔여 예산"
              value={stats?.remainingBudget}
              prefix={<RiseOutlined />}
              suffix="P"
              valueStyle={{ color: '#C8FF00', textShadow: '0 0 2px rgba(0,0,0,0.5)' }} // VoltUp Green
            />
          </Card>
        </Col>
      </Row>

      {/* 추후 그래프 추가 가능 위치 */}
      <div style={{ marginTop: '30px' }}>
      </div>
    </div>
  );
};

export default Dashboard;
