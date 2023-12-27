import { useState } from 'react';

import { useDebouncedValue } from '@mantine/hooks';
import { Table, LoadingOverlay, TextInput, Container } from '@mantine/core';
import '@mantine/core/styles/Container.css';
import '@mantine/core/styles/Table.css';
import '@mantine/core/styles/Input.css';
import { API, useApi } from '../api';

function stringMatch(term: string, text: string) {
    return term.trim() ? text.toLowerCase().includes(term.trim().toLowerCase()) : true;
}

export default function Songs() {
    const [searchTerm, setSearchTerm] = useState('');
    const [debouncedSearchTerm] = useDebouncedValue(searchTerm, 200);
    const { isPending, error, data } = useApi(API.songList);

    if (isPending) return <LoadingOverlay visible={true} zIndex={1000} overlayProps={{ radius: "sm", blur: 2 }} />;

    if (error) return <div>{'An error has occurred: ' + error.message}</div>;

    return (
        <Container>
            <TextInput placeholder="Search" onChange={(event) => setSearchTerm(event.currentTarget.value)} />
            <Table>
                <Table.Thead>
                    <Table.Tr>
                        <Table.Th>Name</Table.Th>
                    </Table.Tr>
                </Table.Thead>
                <Table.Tbody>
                {
                    Object.entries(data)
                    .filter(([ _, songName ]) => stringMatch(debouncedSearchTerm, songName as string))
                    .map(([ key, songName ]) => (
                        <Table.Tr key={key}>
                            <Table.Td data-key={key}>{songName as string}</Table.Td>
                        </Table.Tr>
                    ))
                }
                </Table.Tbody>
            </Table>
        </Container>
    );
}
