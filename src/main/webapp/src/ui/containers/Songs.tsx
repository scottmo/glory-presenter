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
import FileUpload from "../components/FileUpload";

const DEFAULT_LINES_PER_SLIDE = 2;
const DEFAULT_TEMPLATE_PATH = "Error: none found!"

export default function Songs() {
    const [ cacheBustCounter, increaseCacheBustCounter ] = useCacheBustCounter();
    const songListQuery = useQuery(API.songList, { cacheBustCounter });
    const configQuery = useQuery(API.getConfig);

    const [ opened, { open, close } ] = useDisclosure(false);
    const [ songId, setSongId ] = useState("");
    const [ shouldCreateNew, setShouldCreateNew ] = useState(false);
    const [ linesPerSlide, setLinesPerSlide ] = useState<string|number>(DEFAULT_LINES_PER_SLIDE);
    const [ hasStartSlide, toggleStartSlide ] = useState(true);
    const [ hasEndSlide, toggleEndSlide ] = useState(false);
    const defaultTemplatePath = configQuery?.data?.templatePaths
        ?.find((path: string) => path.includes('song'))
        || DEFAULT_TEMPLATE_PATH;
    const [ templatePath, setTemplatePath ] = useState(defaultTemplatePath);

    const handleSelectSong = (row: Row) => {
        setSongId(row.key);
    };

    const handleNewSong = () => {
        setShouldCreateNew(true);
        setSongId("-1");
        open();
    };

    const handleEditSong = () => {
        open();
    };

    const handleDuplicateSong = () => {
        setShouldCreateNew(true);
        open();
    };

    const handleDeleteSong = () => {
        runAction(API.deleteSong, { id: songId });
        increaseCacheBustCounter();
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
        const newSong = shouldCreateNew
            ? Object.assign({}, song, { id: "-1" })
            : song;
        runAction(API.saveSong, {}, newSong);
        close();
        increaseCacheBustCounter();
    };

    const handleSongImported = (errMsg: string) => {
        if (!errMsg) {
            increaseCacheBustCounter();
        } else {
            // TODO
        }
    };

    const isPending = songListQuery.isPending || configQuery.isPending;
    if (isPending) return <LoadingOverlay visible={true} zIndex={1000} overlayProps={{ radius: "sm", blur: 2 }} />;

    const error = songListQuery.error || configQuery.error;
    if (error) return <div>{"An error has occurred: " + error.message}</div>;

    return (
        <>
            <Flex justify="center" align="flex-start" direction="row" wrap="wrap" gap="md" >
                <Flex direction="column" gap="md" w={650} >
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
                    <Button fullWidth onClick={handleDuplicateSong}>Duplicate</Button>
                    <Button fullWidth onClick={handleDeleteSong}>Delete</Button>
                    <Divider />
                    <FileUpload fullWidth label="Import" uploadAPI={API.importSongs} onUpload={handleSongImported} />
                    <Button fullWidth onClick={handleExportSong}>Export</Button>
                    <Divider />
                    <NumberInput label="Lines Per Slide" placeholder="1 to 10" min={1} max={10}
                            value={linesPerSlide} onChange={setLinesPerSlide} />
                    <Select label="PPT Template" placeholder="Pick a template"
                            data={configQuery?.data?.templatePaths}
                            value={templatePath} onChange={setTemplatePath} />
                    <Checkbox label="has start slide"
                            checked={hasStartSlide} onChange={(e) => toggleStartSlide(e.target.checked)} />
                    <Checkbox label="has end slide"
                            checked={hasEndSlide} onChange={(e) => toggleEndSlide(e.target.checked)} />
                    <Button fullWidth onClick={handleGeneratePPTX}>Generate PPTX</Button>
                    <Button fullWidth onClick={handleGenerateGSlides}>Generate Google Slides</Button>
                </Flex>
            </Flex>
            <Modal title="Edit Song" centered size="xl"
                    opened={opened} onClose={close} closeOnClickOutside={false}>
                <SongEditor song={{ id: songId }} locales={configQuery?.data?.locales}
                        onSubmit={handleSubmitSong}/>
            </Modal>
        </>
    );
}
