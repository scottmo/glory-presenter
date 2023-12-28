import { useState } from 'react';
import { QueryAPI, useApi } from '../api';

import { LoadingOverlay, Container } from '@mantine/core';
import '@mantine/core/styles/Container.css';
import '@mantine/core/styles/LoadingOverlay.css';
import DataTable from '../components/DataTable';

export default function Songs() {
    const { isPending, error, data } = useApi(QueryAPI.songList);

    if (isPending) return <LoadingOverlay visible={true} zIndex={1000} overlayProps={{ radius: "sm", blur: 2 }} />;

    if (error) return <div>{'An error has occurred: ' + error.message}</div>;

    return (
        <Container>
            <DataTable
                headers={['Name']}
                rows={Object.entries(data).map(([key, songName]) => ({
                    key, columns: [{ label: songName }]
                }))}
            />
        </Container>
    );
}
