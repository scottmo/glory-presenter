import { Button, Divider, Flex, LoadingOverlay, Select, TextInput } from "@mantine/core";
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

import { API, downloadFile, useCacheBustCounter, useQuery } from "../api";
import FileUpload from "../components/FileUpload";

const DEFAULT_TEMPLATE_PATH = "Error: none found!"

export default function Bible() {
    const [ cacheBustCounter, increaseCacheBustCounter ] = useCacheBustCounter();
    const bibleVersionsQuery = useQuery(API.bibleVersions, { cacheBustCounter });
    const bibleBooksQuery = useQuery(API.bibleBooks);
    const configQuery = useQuery(API.getConfig);

    const defaultTemplatePath = configQuery?.data?.templatePaths
        ?.find((path: string) => path.includes('song'))
        || DEFAULT_TEMPLATE_PATH;
    const [ templatePath, setTemplatePath ] = useState(defaultTemplatePath);
    const [ bibleRef, setBibleRef ] = useState("");

    const handleGeneratePPTX = () => {
        downloadFile(API.generateBiblePPTX, { bibleRef, templatePath });
    };

    const handleGenerateGSlides = () => {
        // TODO
    };

    const handleBibleImported = (errMsg: string) => {
        if (!errMsg) {
            increaseCacheBustCounter();
        } else {
            // TODO
        }
    };

    const isPending = bibleVersionsQuery.isPending || bibleBooksQuery.isPending || configQuery.isPending;
    if (isPending) return <LoadingOverlay visible={true} zIndex={1000} overlayProps={{ radius: "sm", blur: 2 }} />;

    const error = bibleVersionsQuery.error || bibleBooksQuery.error || configQuery.error;
    if (error) return <div>{"An error has occurred: " + error.message}</div>;

    return (
        <Flex justify="center" align="flex-start" direction="row" wrap="wrap" gap="md" >
            <p>Available Versions: {bibleVersionsQuery?.data?.join(", ")}</p>
            <FileUpload label="Import" uploadAPI={API.importBible} onUpload={handleBibleImported} />
            <Divider />
            <p>Book Keys: {bibleBooksQuery?.data?.join(", ")}</p>
            <TextInput label="Verses" description="e.g. john 1:1-2,3:2-4; mark 5:2-3"
                placeholder="Enter verses to generate slides for"
                onChange={e => setBibleRef(e.target.value)}/>
            <Select label="PPT Template" data={configQuery?.data?.templatePaths} placeholder="Pick a template"
                value={templatePath} onChange={setTemplatePath} />
            <Button fullWidth onClick={handleGeneratePPTX}>Generate PPTX</Button>
            <Button fullWidth onClick={handleGenerateGSlides}>Generate Google Slides</Button>
        </Flex>
    );
}
