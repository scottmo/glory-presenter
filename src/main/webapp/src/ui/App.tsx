import { useState } from 'react';
import { AppShell, Burger, Group, NavLink, Code } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { IconCross, } from '@tabler/icons-react';
import { Link, Outlet } from "react-router-dom";
import routes from './routes';

// base components css
import '@mantine/core/styles/ScrollArea.css';
import '@mantine/core/styles/UnstyledButton.css';
import '@mantine/core/styles/VisuallyHidden.css';
import '@mantine/core/styles/Paper.css';
import '@mantine/core/styles/Popover.css';
import '@mantine/core/styles/CloseButton.css';
import '@mantine/core/styles/Group.css';
import '@mantine/core/styles/Loader.css';
import '@mantine/core/styles/Overlay.css';
import '@mantine/core/styles/ModalBase.css';
import '@mantine/core/styles/Input.css';
import '@mantine/core/styles/Flex.css';

import '@mantine/core/styles/NavLink.css';
import '@mantine/core/styles/AppShell.css';
import '@mantine/core/styles/Burger.css';

import classes from './App.module.css';

export default function App() {
    const [active, setActive] = useState('Songs');
    const [opened, { toggle }] = useDisclosure();

    const links = routes.map((item) => (
        <NavLink key={item.label} label={item.label}
            className={classes.link}
            active={item.label === active}
            component={Link} to={item.path}
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
