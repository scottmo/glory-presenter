import type { Song, SongVerse } from '../../types';

import { Button, Grid, Group, Input, LoadingOverlay, Tabs, Text, Textarea } from '@mantine/core';
import { useForm } from '@mantine/form';

import '@mantine/core/styles/Button.css';
import '@mantine/core/styles/Grid.css';
import '@mantine/core/styles/Group.css';
import '@mantine/core/styles/Input.css';
import '@mantine/core/styles/LoadingOverlay.css';
import '@mantine/core/styles/Tabs.css';
import '@mantine/core/styles/Text.css';

import { QueryAPI, useApi } from '../api';

const EXAMPLE_LYRICS = `# v1
Holy holy holy,

# v2

# c1
`;

function toStringArray(str: string | null | undefined) {
    if (!str) return [];
    return str.split(',').map(s => s.trim());
}

type Lyrics = {
    title: string;
    verses: string;
    locale: string;
}
function parseVerses(text: string, locale: string): SongVerse[] {
    return text.split('#').map(s => s.trim()).filter(s => !!s).reduce((verses, verseText) => {
        const currentVerses = verseText.split('\n').map(s => s.trim());
        const name = currentVerses.shift();
        if (name) {
            verses.push({
                text: currentVerses.join("\n").trim(),
                name,
                locale,
            });
        }
        return verses;
    }, [] as SongVerse[]);
}
function stringifyVerses(song: Song): Lyrics[] {
    return (song?.titles || []).reduce((lyrics, title) => {
        lyrics.push({
            title: title.text,
            locale: title.locale,
            verses: (song.verses || []).filter(verse => verse.locale === title.locale).reduce((verseText, verse) => {
                return verseText + `# ${verse.name}\n${verse.text}\n\n`;
            }, ''),
        });
        return lyrics;
    }, [] as Lyrics[]);
}

type Props = {
    song: Song;
    onSubmit: (song: Song) => void
}

export default function SongEditor({ song, onSubmit }: Props) {
    const { isPending, error, data } = useApi(QueryAPI.song, { id: song.id }, { enabled: !!song.id, cacheTime: 0 });

    if (isPending) return <LoadingOverlay visible={true} zIndex={1000} overlayProps={{ radius: "sm", blur: 2 }} />;

    if (error) return <div>{'Unable to load song: ' + error.message}</div>;

    return (
        <SongForm song={data} onSubmit={onSubmit} />
    );
}

export function SongForm({ song, onSubmit }: Props) {
    const id = song.id;
    let lyrics = stringifyVerses(song);
    if (lyrics.length === 0) {
        // TODO fetch default locales from server
        lyrics = [{ locale: 'zh_cn', title: '歌名' }, { locale: 'en_us', title: 'title' }] as Lyrics[];
    }
    const form = useForm({
        initialValues: {
            authors: song?.authors?.join(', ') || '',
            publisher: song?.publisher || '',
            copyright: song?.copyright || '',
            songBook: song?.songBook || '',
            entry: song?.entry || '',
            comments: song?.comments || '',
            verseOrder: song?.verseOrder?.join(', ') || '',
            lyrics,
        }
    });

    const handleSubmit = form.onSubmit(({ authors, publisher, copyright, songBook, entry, comments,
            verseOrder, lyrics }) => {
        const song: Song = {
            id,
            publisher, copyright, songBook, entry, comments,
            authors: toStringArray(authors),
            verseOrder: toStringArray(verseOrder),
        };
        song.titles = lyrics.map(({ locale, title }) => ({ locale, text: title }));
        song.verses = lyrics.map(({ locale, verses }) => parseVerses(verses, locale))
            .reduce((verses, localizedVerses) => {
                return verses.concat(localizedVerses);
            }, []);
        onSubmit(song);
    });

    return (
        <form onSubmit={handleSubmit}>
            <Input.Wrapper label="Authors">
                <Input {...form.getInputProps('authors')} />
            </Input.Wrapper>
            <Grid gutter="xs">
                <Grid.Col span={6}>
                    <Input.Wrapper label="Publisher">
                        <Input {...form.getInputProps('publisher')} />
                    </Input.Wrapper>
                </Grid.Col>
                <Grid.Col span={6}>
                    <Input.Wrapper label="Copyright">
                        <Input {...form.getInputProps('copyright')} />
                    </Input.Wrapper>
                </Grid.Col>
            </Grid>
            <Grid gutter="xs">
                <Grid.Col span={8}>
                    <Input.Wrapper label="Song Book">
                        <Input {...form.getInputProps('songBook')} />
                    </Input.Wrapper>
                </Grid.Col>
                <Grid.Col span={4}>
                    <Input.Wrapper label="Entry">
                        <Input {...form.getInputProps('entry')} />
                    </Input.Wrapper>
                </Grid.Col>
            </Grid>
            <Textarea label="Comments" autosize
                {...form.getInputProps('comments')}
            />
            <Input.Wrapper label="Verse Order">
                <Input {...form.getInputProps('verseOrder')} />
            </Input.Wrapper>
            <br/>
            <Text fw={500} size="sm">
                Lyrics
            </Text>
            <Tabs defaultValue={form.values.lyrics[0].locale}>
                <Tabs.List>
                {form.values.lyrics.map((item, index) => (
                    <Tabs.Tab key={item.locale} value={item.locale}>
                        {item.locale}
                    </Tabs.Tab>
                ))}
                </Tabs.List>
                {form.values.lyrics.map((item, index) => (
                    <Tabs.Panel key={item.locale} value={item.locale}>
                        <Input.Wrapper label="Title">
                            <Input {...form.getInputProps(`lyrics.${index}.title`)} />
                        </Input.Wrapper>
                        <Textarea
                            label="Verses"
                            placeholder={EXAMPLE_LYRICS}
                            rows={10}
                            {...form.getInputProps(`lyrics.${index}.verses`)}
                        />
                    </Tabs.Panel>
                ))}
            </Tabs>
            <Group justify="flex-end" mt="md">
                <Button type="submit">Save</Button>
            </Group>
        </form>
    );
}
