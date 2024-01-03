import { useState } from 'react';
import { QueryAPI, useApi } from '../api';

import { useDisclosure } from '@mantine/hooks';
import { Flex, LoadingOverlay, Modal, Button } from '@mantine/core';
import '@mantine/core/styles/Flex.css';
import '@mantine/core/styles/LoadingOverlay.css';
import '@mantine/core/styles/ModalBase.css';
import '@mantine/core/styles/Modal.css';
import '@mantine/core/styles/Button.css';
import DataTable from '../components/DataTable';
import SongEditor from '../components/SongEditor';

import classes from './Songs.module.css';

export default function Songs() {
    const { isPending, error, data } = useApi(QueryAPI.songList);
    const [opened, { open, close }] = useDisclosure(false);
    const [songId, setSongId] = useState('1');

    if (isPending) return <LoadingOverlay visible={true} zIndex={1000} overlayProps={{ radius: "sm", blur: 2 }} />;

    if (error) return <div>{'An error has occurred: ' + error.message}</div>;

    return (
        <>
            <Button onClick={open}>Open song editor</Button>
            <Flex
                direction={{ base: 'column', sm: 'row' }}
                gap={{ base: 'sm', sm: 'lg' }}
                justify={{ sm: 'center' }}
            >
                <DataTable
                    tableClassName={classes.songTable}
                    headers={['Name']}
                    rows={Object.entries(data).map(([key, songName]) => ({
                        key, columns: [{ label: songName as string }]
                    }))}
                />
            </Flex>
            <Modal opened={opened} onClose={close} title="Edit Song" centered>
                <SongEditor song={{ id: songId }} onSubmit={(song) => console.log(song)}/>
            </Modal>
        </>
    );
}
