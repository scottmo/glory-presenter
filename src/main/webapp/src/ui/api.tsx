import { useQuery } from '@tanstack/react-query'

const apiOrigin = 'http://localhost:8080/api';

export const API = {
    songList: 'song/titles'
};

export function useApi(path: string) {
    return useQuery({
        queryKey: [path],
        queryFn: () => fetch(`${apiOrigin}/${path}`).then((res) => res.json())
    });
}
