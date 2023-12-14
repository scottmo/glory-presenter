import React from 'react';
import ReactDOM from 'react-dom/client';
import { MantineProvider, createTheme } from '@mantine/core';
import '@mantine/core/styles/global.css';
import reportWebVitals from './reportWebVitals';
import {
    createBrowserRouter,
    RouterProvider,
} from "react-router-dom";
import App from './App';

const theme = createTheme({
    /** Your theme override here */
    primaryColor: 'gcbc-red',
    colors: {
        'gcbc-red': ['#FDEDED', '#F4D7D7', '#EEA9A9', '#E87A79', '#E35350', '#E13B35', '#E02F28', '#C7231D', '#B11C18', '#9B1212'],
    },
});

// import GSlides from './ui/old_components_with_bootstrap/GSlides';
// import GlobalProvider from "../old_components_with_bootstrap/providers/GlobalProvider";

const router = createBrowserRouter([
    {
        path: "/",
        element: <App></App>,
        // children: [
        //     {
        //         path: "slides",
        //         element: <GSlides />,
        //     },
        // ],
    },
]);

const root = ReactDOM.createRoot(
    document.getElementById('root') as HTMLElement
);
root.render(
    <React.StrictMode>
        <MantineProvider theme={theme}>
            {/* <GlobalProvider> */}
            <RouterProvider router={router} />
            {/* </GlobalProvider> */}
        </MantineProvider>
    </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
