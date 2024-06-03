import axios from 'axios';
import { useState } from "react";
import { useQuery as useReactQuery } from '@tanstack/react-query';

const apiOrigin = 'http://localhost:8080/api';

export type ServerAction = {
    method: string;
    path: string;
}
export const API: Record<string, ServerAction> = {
    updateStyles     : { method: 'POST',   path: 'google/updatestyles/:id'},
    getConfig        : { method: 'GET',    path: 'config' },
    saveConfig       : { method: 'POST',   path: 'config' },
    generateSongPPTX : { method: 'GET',    path: 'song/pptx' },
    songList         : { method: 'GET',    path: 'song/titles' },
    song             : { method: 'GET',    path: 'song/:id' },
    exportSong       : { method: 'GET',    path: 'song/export/:id' },
    deleteSong       : { method: 'DELETE', path: 'song/:id' },
    saveSong         : { method: 'POST',   path: 'song/save' },
    importSongs      : { method: 'POST',   path: 'song/import' },
    bibleVersions    : { method: 'GET',    path: 'bible/versions' },
    bibleBooks       : { method: 'GET',    path: 'bible/books' },
    importBible      : { method: 'POST',   path: 'bible/import' },
    generateBiblePPTX: { method: 'GET',    path: 'bible/pptx' },
};

type ApiParams = Record<string, string | number | undefined>;

export function useCacheBustCounter(): [number, () => void] {
    const [cacheBustCounter, setCacheBustCounter] = useState(0);
    const increaseCacheBustCounter = () => {
        setCacheBustCounter(cacheBustCounter + 1);
    };
    return [cacheBustCounter, increaseCacheBustCounter];
}

export function useQuery(path: string | ServerAction, params?: ApiParams, options?: Record<string, any>) {
    path = typeof path === 'string' ? path : path.path;
    const requestUri = generateRequestUri(path, params);
    return useReactQuery({
        queryKey: [requestUri],
        queryFn: () => axios.get(requestUri).then(resp => resp.data),
        ...options,
    });
}

export function runAction(api: ServerAction, params: ApiParams, data?: any) {
    const requestUri = generateRequestUri(api.path, params);
    switch (api.method) {
        case 'GET':
            return axios.get(requestUri);
        case 'POST':
            return axios.post(requestUri, data);
        case 'DELETE':
            return axios.delete(requestUri, data);
        default:
            throw new Error('Invalid action!');
    }
}

export function downloadFile(path: string | ServerAction, params?: ApiParams) {
    window.open(generateRequestUri(path, params), '_blank');
}

function extractParams(path: string) {
    return (path.match(/:\w+/g) || []).map(token => token.substring(1));
}

function generateRequestUri(path: string | ServerAction, params?: ApiParams) {
    let requestUri = typeof path === 'string' ? path : path.path;
    params = Object.assign({}, params);
    // fill route params
    const paramKeys = extractParams(requestUri);
    for (let key of paramKeys) {
        if (params[key]) {
            requestUri = requestUri.replace(':' + key, params[key] as string);
            delete params[key];
        }
    }
    // put the rest as query params
    if (Object.keys(params).length > 0) {
        requestUri += '?' + Object.entries(params)
            .map(([k, v]) => typeof v !== 'object' ? `${k}=${v}` : '')
            .filter(s => !!s)
            .join('&');
    }
    return `${apiOrigin}/${requestUri}`;
}
