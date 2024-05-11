import { FileInput } from '@mantine/core';
import { ServerAction, runAction } from '../api';

import classes from './FileUpload.module.css';

type Props = {
    label: string;
    fullWidth?: boolean;
    uploadAPI: ServerAction;
    onUpload?: (errMsg: string) => void;
};

export default function FileUpload({ label, fullWidth = false, uploadAPI, onUpload }: Props) {
    const handleUpload = async (file: File | null) => {
        const response = await runAction(uploadAPI, {}, file);
        if (response.data.status !== "ok") {
            onUpload?.(response.data.message);
        } else {
            onUpload?.("");
        }
    };

    return (
        <FileInput variant="filled" label={label} placeholder="Upload"
                className={fullWidth ? classes.fullWidth : ""}
                onChange={handleUpload} />
    );
}
