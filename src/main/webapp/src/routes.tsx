import {
    IconBible,
    IconBrandGoogle,
    IconSettings,
    IconSlideshow,
    IconMusic,
} from '@tabler/icons-react';
import Songs from './ui/containers/Songs';
import Settings from './ui/containers/Settings';

export default [
    { path: '/songs', label: 'Songs', icon: IconMusic, component: Songs },
    { path: '/bible', label: 'Bible', icon: IconBible, component: Songs },
    { path: '/gslides', label: 'Google Slides', icon: IconBrandGoogle, component: Songs },
    { path: '/ppt', label: 'Powerpoint', icon: IconSlideshow, component: Songs },
    { path: '/settings', label: 'Settings', icon: IconSettings, component: Settings }
];
