import { useState } from 'react';
import { AppShell, Burger, Group, NavLink, Code } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import {
    IconBible,
    IconBrandGoogle,
    IconSettings,
    IconSlideshow,
    IconMusic,
    IconCross,
} from '@tabler/icons-react';
import {
    Link, Outlet
} from "react-router-dom";

import '@mantine/core/styles/UnstyledButton.css';
import '@mantine/core/styles/NavLink.css';
import '@mantine/core/styles/AppShell.css';
import '@mantine/core/styles/Burger.css';
import classes from './App.module.css';

const data = [
    { link: '/songs', label: 'Songs', icon: IconMusic },
    { link: '/bible', label: 'Bible', icon: IconBible },
    { link: '/gslides', label: 'Google Slides', icon: IconBrandGoogle },
    { link: '/ppt', label: 'Powerpoint', icon: IconSlideshow },
    { link: '/settings', label: 'Settings', icon: IconSettings }
];

export default function App() {
    const [active, setActive] = useState('Songs');
    const [opened, { toggle }] = useDisclosure();

    const links = data.map((item) => (
        <NavLink key={item.label} label={item.label}
            className={classes.link}
            active={item.label === active}
            component={Link} to={item.link}
            onClick={(event) => setActive(item.label)}
            leftSection={<item.icon size="1rem" stroke={1.5} />} />
    ));

    return (
        <AppShell
            header={{ height: { base: 60, md: 70, lg: 80 } }}
            navbar={{
                width: { base: 200, md: 300, lg: 400 },
                breakpoint: 'sm',
                collapsed: { mobile: !opened },
            }}
            padding="md"
        >
            <AppShell.Header>
                <Group h="100%" px="md">
                    <Burger opened={opened} onClick={toggle} hiddenFrom="sm" size="sm" />
                    <IconCross size={45} className={classes.logo} />
                    <Code fw={700}>v1.0</Code>
                </Group>
            </AppShell.Header>
            <AppShell.Navbar p="md">
                <div>{links}</div>
            </AppShell.Navbar>
            <AppShell.Main><Outlet /></AppShell.Main>
        </AppShell>
    );
}
