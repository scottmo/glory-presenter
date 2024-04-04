import { Button, Checkbox, Divider, Flex,
    LoadingOverlay, Modal, NumberInput, Select, ScrollArea } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { useState } from "react";

import "@mantine/core/styles/Button.css";
import "@mantine/core/styles/Checkbox.css";
import '@mantine/core/styles/Combobox.css';
import '@mantine/core/styles/Divider.css';
import "@mantine/core/styles/Flex.css";
import "@mantine/core/styles/Input.css";
import "@mantine/core/styles/LoadingOverlay.css";
import "@mantine/core/styles/Modal.css";
import "@mantine/core/styles/ModalBase.css";
import "@mantine/core/styles/NumberInput.css";

import type { Song } from '../../types';
import { API, downloadFile, runAction, useQuery, useCacheBustCounter } from "../api";
import DataTable, { Row } from "../components/DataTable";
import SongEditor from "../components/SongEditor";

const DEFAULT_LINES_PER_SLIDE = 2;
const DEFAULT_TEMPLATE_PATH = "Error: none found!"

export default function Songs() {
    const [ cacheBustCounter, increaseCacheBustCounter ] = useCacheBustCounter();
    const songListQuery = useQuery(API.songList, { cacheBustCounter });
    const configQuery = useQuery(API.getConfig);

    const [ opened, { open, close } ] = useDisclosure(false);
    const [ songId, setSongId ] = useState("");
    const [ linesPerSlide, setLinesPerSlide ] = useState<string|number>(DEFAULT_LINES_PER_SLIDE);
    const [ templatePath, setTemplatePath ] = useState(configQuery?.data?.templatePaths?.[0] || DEFAULT_TEMPLATE_PATH);
    const [ hasStartSlide, toggleStartSlide ] = useState(true);
    const [ hasEndSlide, toggleEndSlide ] = useState(false);

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
        runAction(API.deleteSong, { id: songId });
        increaseCacheBustCounter();
    };

    const handleImportSongs = () => {
        // TODO
    };

    const handleExportSong = () => {
        downloadFile(API.exportSong, { id: songId });
    };

    const handleGeneratePPTX = () => {
        downloadFile(API.generateSongPPTX, { id: songId, linesPerSlide, templatePath });
    };

    const handleGenerateGSlides = () => {
        // TODO
    };

    const handleSubmitSong = (song: Song) => {
        runAction(API.saveSong, {}, song);
        increaseCacheBustCounter();
    };

    const isPending = songListQuery.isPending || configQuery.isPending;
    if (isPending) return <LoadingOverlay visible={true} zIndex={1000} overlayProps={{ radius: "sm", blur: 2 }} />;

    const error = songListQuery.error || configQuery.error;
    if (error) return <div>{"An error has occurred: " + error.message}</div>;

    return (
        <>
            <Flex justify="center" align="flex-start" direction="row" wrap="wrap" gap="md" >
                <Flex direction="column" gap="md" >
                    <ScrollArea h={document.body.offsetHeight - 100}>
                        <DataTable
                            headers={["Name"]}
                            onRowClick={handleSelectSong}
                            rows={Object.entries(songListQuery.data).map(([key, songName]) => ({
                                key, columns: [{ label: songName as string }]
                            }))}
                        />
                    </ScrollArea>
                </Flex>
                <Flex direction="column" justify="flex-start" align="flex-start" gap="xs" >
                    <p>Total # of Songs: { Object.entries(songListQuery.data).length }</p>
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
                    <Select label="PPT Template" data={configQuery?.data?.templatePaths} placeholder="Pick a template"
                        value={templatePath} onChange={(value) => setTemplatePath(value)} />
                    <Checkbox label="has start slide"
                        checked={hasStartSlide} onChange={() => toggleStartSlide(!hasStartSlide)} />
                    <Checkbox label="has end slide"
                        checked={hasEndSlide} onChange={() => toggleEndSlide(!hasEndSlide)} />
                    <Button fullWidth onClick={handleGeneratePPTX}>Generate PPTX</Button>
                    <Button fullWidth onClick={handleGenerateGSlides}>Generate Google Slides</Button>
                </Flex>
            </Flex>
            <Modal opened={opened} onClose={close} title="Edit Song" centered>
                <SongEditor song={{ id: songId }} locales={configQuery?.data?.locales} onSubmit={handleSubmitSong}/>
            </Modal>
        </>
    );
}
