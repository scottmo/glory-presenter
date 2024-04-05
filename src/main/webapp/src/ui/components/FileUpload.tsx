import { FileInput } from '@mantine/core';
import { ServerAction, runAction } from '../api';

type Props = {
    label: string;
    uploadAPI: ServerAction;
};

export default function FileUpload({ label, uploadAPI }: Props) {
    const handleUpload = async (file: File | null) => {
        const response = await runAction(uploadAPI, {}, file);
        if (response.data.status !== "ok") {
            // TODO
        }
    };

    return (
        <FileInput variant="filled" label={label} placeholder="Upload" onChange={handleUpload} />
    );
}
