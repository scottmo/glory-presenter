import { useQuery } from '@tanstack/react-query';
import axios from 'axios';

const apiOrigin = 'http://localhost:8080/api';

export const QueryAPI = {
    songList     : 'song/titles',
    song         : 'song/:id',
    bibleVersions: 'bible/versions',
    bibleBooks   : 'bible/books',
};

type ServerAction = {
    method: string;
    path: string;
}
export const ActionAPI: Record<string, ServerAction> = {
    generateSongPPTX : { method: 'GET',  path: 'song/pptx' },
    exportSong       : { method: 'GET',  path: 'song/export/:id' },
    deleteSong       : { method: 'DELETE', path: 'song/:id' },
    saveSong         : { method: 'POST', path: 'song/save' },
    importSongs      : { method: 'POST', path: 'song/import' },
    importBible      : { method: 'POST', path: 'bible/import' },
    generateBiblePPTX: { method: 'GET',  path: 'bible/pptx' },
};

export function useApi(path: string, params?: Record<string, string | undefined>, options?: Record<string, any>) {
    const requestUri = generateRequestUri(path, params);
    return useQuery({
        queryKey: [requestUri],
        queryFn: () => axios.get(requestUri).then(resp => resp.data),
        ...options,
    });
}

export function runAction(api: ServerAction, params: any, data?: any) {
    const requestUri = generateRequestUri(api.path, params);
    if (api.method === 'GET') {
        return axios.get(requestUri);
    }
    return axios.post(requestUri, data);
}

function extractParams(path: string) {
    return (path.match(/:\w+/g) || []).map(token => token.substring(1));
}

function generateRequestUri(path: string, params?: Record<string, string | undefined>) {
    let requestUri = path;
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
        requestUri += '?' + Object.entries(params).map(([k, v]) => `${k}=${v}`).join('&');
    }
    return `${apiOrigin}/${requestUri}`;
}
