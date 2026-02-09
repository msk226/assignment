import { Table, Button, Popconfirm, message, Spin, Tag, Tooltip, Alert } from 'antd';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { rouletteApi, RouletteParticipation } from '../api/endpoints';

const Roulette: React.FC = () => {
    const queryClient = useQueryClient();
    const [messageApi, contextHolder] = message.useMessage();

    const { data: participations, isLoading } = useQuery({
        queryKey: ['rouletteList'],
        queryFn: rouletteApi.getRouletteList,
    });

    const cancelMutation = useMutation({
        mutationFn: rouletteApi.cancelRoulette,
        onSuccess: (data: any) => {
            messageApi.success(data.message || '참여가 취소되었습니다.');
            queryClient.invalidateQueries({ queryKey: ['rouletteList'] });
            queryClient.invalidateQueries({ queryKey: ['dashboardStats'] });
        },
        onError: (error: any) => {
            messageApi.error(error.message || '참여 취소 실패');
        }
    });

    const columns = [
        { title: 'ID', dataIndex: 'id', key: 'id' },
        { title: '유저 ID', dataIndex: 'userId', key: 'userId' },
        { title: '닉네임', dataIndex: 'nickname', key: 'nickname' },
        { title: '획득 포인트', dataIndex: 'points', key: 'points', render: (val: number) => `${val.toLocaleString()} P` },
        {
            title: '상태',
            dataIndex: 'status',
            key: 'status',
            render: (val: string | undefined, record: RouletteParticipation) => {
                // 백엔드에서 status필드가 오지 않을 경우 cancelledAt으로 판단하는 폴백 로직 추가
                const status = val || (record.cancelledAt ? 'CANCELLED' : 'PARTICIPATED');
                const color = status === 'CANCELLED' ? 'red' : '#C8FF00';
                const style = status !== 'CANCELLED' ? { color: 'black', fontWeight: 'bold' } : {};
                return (
                    <Tooltip title={record.cancelledAt ? `취소일시: ${new Date(record.cancelledAt).toLocaleString()}` : ''}>
                        <Tag color={color} style={style}>{status}</Tag>
                    </Tooltip>
                );
            }
        },
        { title: '참여 일시', dataIndex: 'createdAt', key: 'createdAt', render: (val: string) => new Date(val).toLocaleString() },
        {
            title: '관리',
            key: 'action',
            render: (_: any, record: RouletteParticipation) => {
                const isCancelled = record.status === 'CANCELLED' || !!record.cancelledAt;
                return (
                    <Popconfirm
                        title="참여를 취소하고 포인트를 회수하시겠습니까?"
                        onConfirm={() => cancelMutation.mutate(record.id)}
                        disabled={isCancelled}
                    >
                        <Button
                            danger
                            size="small"
                            loading={cancelMutation.isPending && cancelMutation.variables === record.id}
                            disabled={isCancelled}
                        >
                            {isCancelled ? '취소됨' : '참여 취소'}
                        </Button>
                    </Popconfirm>
                );
            },
        },
    ];

    return (
        <div>
            {contextHolder}
            <h2 style={{ marginBottom: 16 }}>룰렛 참여 관리</h2>
            <Alert
                message="오늘 참여된 룰렛만 취소할 수 있습니다."
                type="info"
                showIcon
                style={{ marginBottom: 16 }}
            />
            {isLoading ? <Spin /> : <Table dataSource={participations} columns={columns} rowKey="id" />}
        </div>
    );
};

export default Roulette;
