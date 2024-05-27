
import { Button, NumberInput, Tabs, TextInput, Textarea } from "@mantine/core";
import { useState } from "react";

function extractPresentationId(url: string) {
    try {
        const { pathname } = new URL(url);
        return pathname.substring(pathname.indexOf("/d/") + 3, pathname.lastIndexOf("/"));
    } catch (e) {
        // ignore invalid input
    }
    return null;
}

const CONFIGURATION_EXAMPLE = `# zh_cn
alignment: CENTER
fontColor: 255, 255, 255
fontFamily: STKaiti
fontSize: 60
fontStyles: bold

# en_us
alignment: CENTER
fontColor: 255, 255, 153
fontFamily: Arial Narrow
fontSize: 52
fontStyles: bold
`;

export default function GSlide() {
    const [ pptId, setPptId ] = useState("");
    const [ content, setContent ] = useState("");
    const [ startIndex, setStartIndex ] = useState(0);
    const [ endIndex, setEndIndex ] = useState(999);

    const handlePresentationURL = (value: string) => {
        const pptId = extractPresentationId(value);
        if (pptId) {
            setPptId(pptId);
        }
    };

    const handlePresentationGeneration = () => {

    };

    const handlePresentationUpdate = () => {

    };

    return <>
        <TextInput label="Presentation ID/URL" onChange={e => handlePresentationURL(e.target.value)}/>
        <Textarea label="Configuration" rows={10} defaultValue={CONFIGURATION_EXAMPLE} />

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
