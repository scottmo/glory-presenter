import { useState } from 'react';

import { useDebouncedValue } from '@mantine/hooks';
import { Table, Input, Container } from '@mantine/core';
import '@mantine/core/styles/Container.css';
import '@mantine/core/styles/Input.css';
import '@mantine/core/styles/Table.css';

type Column = {
    label: string;
}

type Row = {
    key: string;
    columns: Column[];
}

type Props = {
    headers: string[];
    rows: Row[];
}

function stringMatch(term: string, text: string) {
    return term.trim() ? text.toLowerCase().includes(term.trim().toLowerCase()) : true;
}

export default function DataTable({ headers, rows }: Props) {
    const [searchTerm, setSearchTerm] = useState('');
    const [debouncedSearchTerm] = useDebouncedValue(searchTerm, 200);

    const filteredRows = rows.filter(row => {
        const searchKey = row.columns.reduce((acc, curr) => acc + " " + curr.label, "");
        return stringMatch(debouncedSearchTerm, searchKey);
    });

    return (
        <Container>
            <Input.Wrapper label="Search">
                <Input placeholder="Holy holy holy"
                    onChange={(event) => setSearchTerm(event.currentTarget.value)} />
            </Input.Wrapper>
            <Table>
                <Table.Thead>
                    <Table.Tr>
                    {headers.map(header => (
                        <Table.Th>
                            {header}
                        </Table.Th>
                    ))}
                    </Table.Tr>
                </Table.Thead>
                <Table.Tbody>
                {filteredRows.map(row => (
                    <Table.Tr key={row.key}>
                    {row.columns.map(column => (
                        <Table.Td key={column.label} data-key={row.key + column.label}>
                            {column.label}
                        </Table.Td>
                    ))}
                    </Table.Tr>
                ))}
                </Table.Tbody>
            </Table>
        </Container>
    );
}
