
import { Button, NumberInput, Tabs, TextInput, Textarea } from "@mantine/core";
import { JsonEditor } from "json-edit-react";
import { useState } from "react";
import { API, downloadFile, runAction, useQuery, useCacheBustCounter } from "../api";

function extractPresentationId(url: string) {
    try {
        const { pathname } = new URL(url);
        return pathname.substring(pathname.indexOf("/d/") + 3, pathname.lastIndexOf("/"));
    } catch (e) {
        // ignore invalid input
    }
    return null;
}

const CONFIGURATION_EXAMPLE = {
    x: 0,
    y: 0,
    indentation: 28,
    alignment: "CENTER",
    fontConfigs: {
        zh_cn: {
            fontColor: "255, 255, 255",
            fontFamily: "STKaiti",
            fontSize: 60,
            fontStyles: "bold",
        },
        en_us: {
            fontColor: "255, 255, 153",
            fontFamily: "Arial Narrow",
            fontSize: 52,
            fontStyles: "bold",
        }
    }
};

export default function GSlide() {
    const [ pptId, setPptId ] = useState("");
    const [ content, setContent ] = useState("");
    const [ startIndex, setStartIndex ] = useState(0);
    const [ endIndex, setEndIndex ] = useState(999);
    const [ slideConfig, setSlideConfig ] = useState(CONFIGURATION_EXAMPLE as object);

    const handlePresentationURL = (value: string) => {
        const pptId = extractPresentationId(value);
        if (pptId) {
            setPptId(pptId);
        }
    };

    const handlePresentationGeneration = () => {

    };

    const handlePresentationUpdate = () => {
        runAction(API.updateStyles, { id: pptId, slideConfig: JSON.stringify(slideConfig), startIndex, endIndex });
    };

    return <>
        <TextInput label="Presentation ID/URL" onChange={e => handlePresentationURL(e.target.value)}/>
        <JsonEditor data={slideConfig} rootName="slideConfig" indent={4}
            showStringQuotes={false}
            restrictDelete={true}
            restrictAdd={({ level }) => level > 1 /* only allow adding font configs */ }
            onUpdate={({ newData }) => setSlideConfig(newData)}
        />

        <Tabs defaultValue="generate">
            <Tabs.List>
                <Tabs.Tab value="generate">
                    Generate
                </Tabs.Tab>
                <Tabs.Tab value="theming">
                    Theming
                </Tabs.Tab>
            </Tabs.List>
            <Tabs.Panel value="generate">
                <NumberInput label="Insert Location (Slide Number)" min={0} defaultValue={0}
                        onChange={v => setStartIndex(Number(v))}/>
                <TextInput label="Content" onChange={e => setContent(e.target.value)}/>
                <Button onClick={handlePresentationGeneration} disabled={!!content}>Generate</Button>
            </Tabs.Panel>
            <Tabs.Panel value="theming">
                <NumberInput label="From Location (Slide Number)" min={0} defaultValue={0}
                        onChange={v => setStartIndex(Number(v))}/>
                <NumberInput label="To Location (Slide Number)" defaultValue={999} min={0}
                        onChange={v => setEndIndex(Number(v))}/>
                <Button onClick={handlePresentationUpdate}>Update</Button>
            </Tabs.Panel>
        </Tabs>
    </>
}
