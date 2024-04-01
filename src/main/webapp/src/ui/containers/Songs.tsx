import { useState } from "react";
import { QueryAPI, ActionAPI, useApi, runAction } from "../api";

import { useDisclosure } from "@mantine/hooks";
import { Input } from "@mantine/core";
import { Flex, LoadingOverlay, Modal, Button, NumberInput, Checkbox, Divider } from "@mantine/core";
import DataTable, { Row } from "../components/DataTable";
import SongEditor from "../components/SongEditor";

import "@mantine/core/styles/Flex.css";
import "@mantine/core/styles/Checkbox.css";
import "@mantine/core/styles/Input.css";
import '@mantine/core/styles/Divider.css';
import "@mantine/core/styles/NumberInput.css";
import "@mantine/core/styles/LoadingOverlay.css";
import "@mantine/core/styles/ModalBase.css";
import "@mantine/core/styles/Modal.css";
import "@mantine/core/styles/Button.css";
import classes from "./Songs.module.css";

export default function Songs() {
    const { isPending, error, data } = useApi(QueryAPI.songList);
    const [opened, { open, close }] = useDisclosure(false);
    const [songId, setSongId] = useState("");

    if (isPending) return <LoadingOverlay visible={true} zIndex={1000} overlayProps={{ radius: "sm", blur: 2 }} />;

    if (error) return <div>{"An error has occurred: " + error.message}</div>;

    const handleSelectSong = (row: Row) => {
        setSongId(row.key);
    };

    const handleNewSong = () => {
        setSongId("");
        open();
    };

    const handleEditSong = () => {
        open();
    };

    const handleDeleteSong = () => {
        runAction(ActionAPI.deleteSong, { id: songId });
    };

    return (
        <>
            <Flex justify="center" align="center" direction="row" wrap="wrap" gap="md" >
                <Flex direction="column" gap="md" >
                    <DataTable
                        tableClassName={classes.songTable}
                        headers={["Name"]}
                        onRowClick={handleSelectSong}
                        rows={Object.entries(data).map(([key, songName]) => ({
                            key, columns: [{ label: songName as string }]
                        }))}
                    />
                </Flex>
                <Flex direction="column" justify="flex-start" align="flex-start" gap="xs" >
                    <p>Total # of Songs: { Object.entries(data).length }</p>
                    <Divider />
                    <Button fullWidth onClick={handleNewSong}>New</Button>
                    <Button fullWidth onClick={handleEditSong}>Edit</Button>
                    <Button fullWidth onClick={handleDeleteSong}>Delete</Button>
                    <Divider />
                    <Button fullWidth>Import</Button>
                    <Button fullWidth>Export</Button>
                    <Divider />
                    <NumberInput label="Lines Per Slide" placeholder="1 to 10" min={1} max={10}/>
                    <Input.Wrapper label="PPT Template">
                        <Input />
                    </Input.Wrapper>
                    <Checkbox defaultChecked label="has start slide" />
                    <Checkbox label="has end slide" />
                    <Button fullWidth>Generate PPTX</Button>
                    <Button fullWidth>Generate Google Slides</Button>
                </Flex>
            </Flex>
            <Modal opened={opened} onClose={close} title="Edit Song" centered>
                <SongEditor song={{ id: songId }} onSubmit={(song) => console.log(song)}/>
            </Modal>
        </>
    );
}
