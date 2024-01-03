import { useState } from 'react';
import { QueryAPI, useApi } from '../api';

import { Flex, LoadingOverlay, Container } from '@mantine/core';
import '@mantine/core/styles/Container.css';
import '@mantine/core/styles/Flex.css';
import '@mantine/core/styles/LoadingOverlay.css';
import DataTable from '../components/DataTable';
import SongEditor from '../components/SongEditor';

export default function Songs() {
    const { isPending, error, data } = useApi(QueryAPI.songList);

    if (isPending) return <LoadingOverlay visible={true} zIndex={1000} overlayProps={{ radius: "sm", blur: 2 }} />;

    if (error) return <div>{'An error has occurred: ' + error.message}</div>;

    return (
        <Container>
            <Flex
                direction={{ base: 'column', sm: 'row' }}
                gap={{ base: 'sm', sm: 'lg' }}
                justify={{ sm: 'center' }}
            >
                <DataTable
                    headers={['Name']}
                    rows={Object.entries(data).map(([key, songName]) => ({
                        key, columns: [{ label: songName as string }]
                    }))}
                />
                <SongEditor song={{ id: "1" }} onSubmit={(song) => console.log(song)}/>
            </Flex>
        </Container>
    );
}
