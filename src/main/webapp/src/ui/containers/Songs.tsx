import { useQuery } from '@tanstack/react-query'

import { Table, LoadingOverlay } from '@mantine/core';
import '@mantine/core/styles/Table.css';

export default function Songs() {
    const { isPending, error, data } = useQuery({
        queryKey: ['song/titles'],
        queryFn: () =>
            fetch('/api/song/titles').then(
                (res) => res.json(),
            ),
    });

    if (isPending) return <LoadingOverlay visible={true} zIndex={1000} overlayProps={{ radius: "sm", blur: 2 }} />;

    if (error) return <div>{'An error has occurred: ' + error.message}</div>;

    return (
        <Table>
            <Table.Thead>
                <Table.Tr>
                    <Table.Th>Name</Table.Th>
                </Table.Tr>
            </Table.Thead>
            <Table.Tbody>
            {
                Object.entries(data).map(([ key, songName ]) => (
                    <Table.Tr key={key}>
                        <Table.Td data-key={key}>{songName as string}</Table.Td>
                    </Table.Tr>
                ))
            }
            </Table.Tbody>
        </Table>
    );
}
