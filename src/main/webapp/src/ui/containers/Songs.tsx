import { Button, Checkbox, Divider, Flex, LoadingOverlay, Modal, NumberInput, TextInput } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { useState } from "react";

import "@mantine/core/styles/Button.css";
import "@mantine/core/styles/Checkbox.css";
import '@mantine/core/styles/Divider.css';
import "@mantine/core/styles/Flex.css";
import "@mantine/core/styles/Input.css";
import "@mantine/core/styles/LoadingOverlay.css";
import "@mantine/core/styles/Modal.css";
import "@mantine/core/styles/ModalBase.css";
import "@mantine/core/styles/NumberInput.css";

import type { Song } from '../../types';
import { ActionAPI, QueryAPI, downloadFile, runAction, useApi, useCacheBustCounter } from "../api";
import DataTable, { Row } from "../components/DataTable";
import SongEditor from "../components/SongEditor";

import classes from "./Songs.module.css";

export default function Songs() {
    const [cacheBustCounter, increaseCacheBustCounter] = useCacheBustCounter();
    const { isPending, error, data } = useApi(QueryAPI.songList, { cacheBustCounter });
    const [opened, { open, close }] = useDisclosure(false);
    const [songId, setSongId] = useState("");
    const [linesPerSlide, setLinesPerSlide] = useState<string|number>(2);
    const [templatePath, setTemplatePath] = useState("template-song.pptx");
    const [hasStartSlide, toggleStartSlide] = useState(true);
    const [hasEndSlide, toggleEndSlide] = useState(false);

    const handleSelectSong = (row: Row) => {
        setSongId(row.key);
    };

    const handleNewSong = () => {
        setSongId("-1");
        open();
    };

    const handleEditSong = () => {
        open();
    };

    const handleDeleteSong = () => {
        runAction(ActionAPI.deleteSong, { id: songId });
    };

    const handleImportSongs = () => {
        // TODO
    };

    const handleExportSong = () => {
        downloadFile(ActionAPI.exportSong, { id: songId });
    };

    const handleGeneratePPTX = () => {
        downloadFile(ActionAPI.generateSongPPTX, { id: songId, linesPerSlide, templatePath });
    };

    const handleGenerateGSlides = () => {
        // TODO
    };

    const handleSetTemplatePath = (e: React.ChangeEvent<HTMLInputElement>) => {
        setTemplatePath(e.target.value);
    };

    const handleSubmitSong = (song: Song) => {
        runAction(ActionAPI.saveSong, {}, song);
        increaseCacheBustCounter();
    };

    const handleToggleStartSlide = () => toggleStartSlide(!hasStartSlide);
    const handleToggleEndSlide = () => toggleEndSlide(!hasEndSlide);

    if (isPending) return <LoadingOverlay visible={true} zIndex={1000} overlayProps={{ radius: "sm", blur: 2 }} />;

    if (error) return <div>{"An error has occurred: " + error.message}</div>;

    return (
        <>
            <Flex justify="center" align="flex-start" direction="row" wrap="wrap" gap="md" >
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
                    <Button fullWidth onClick={handleImportSongs}>Import</Button>
                    <Button fullWidth onClick={handleExportSong}>Export</Button>
                    <Divider />
                    <NumberInput label="Lines Per Slide" placeholder="1 to 10" min={1} max={10}
                        value={linesPerSlide} onChange={setLinesPerSlide} />
                    <TextInput label="PPT Template"
                        value={templatePath} onChange={handleSetTemplatePath} />
                    <Checkbox label="has start slide"
                        checked={hasStartSlide} onChange={handleToggleStartSlide} />
                    <Checkbox label="has end slide"
                        checked={hasEndSlide} onChange={handleToggleEndSlide} />
                    <Button fullWidth onClick={handleGeneratePPTX}>Generate PPTX</Button>
                    <Button fullWidth onClick={handleGenerateGSlides}>Generate Google Slides</Button>
                </Flex>
            </Flex>
            <Modal opened={opened} onClose={close} title="Edit Song" centered>
                <SongEditor song={{ id: songId }} onSubmit={handleSubmitSong}/>
            </Modal>
        </>
    );
}
