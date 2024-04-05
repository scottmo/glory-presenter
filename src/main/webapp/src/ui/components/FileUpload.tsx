import { FileInput } from '@mantine/core';
import { ServerAction, runAction } from '../api';

type Props = {
    label: string;
    uploadAPI: ServerAction;
    onUpload?: (errMsg: string) => void;
};

export default function FileUpload({ label, uploadAPI, onUpload }: Props) {
    const handleUpload = async (file: File | null) => {
        const response = await runAction(uploadAPI, {}, file);
        if (response.data.status !== "ok") {
            onUpload?.(response.data.message);
        } else {
            onUpload?.("");
        }
    };

    return (
        <FileInput variant="filled" label={label} placeholder="Upload" onChange={handleUpload} />
    );
}
