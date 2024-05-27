import {
    IconBible,
    IconBrandGoogle,
    IconMusic,
    IconSettings,
    IconSlideshow,
} from '@tabler/icons-react';
import Bible from './containers/Bible';
import Settings from './containers/Settings';
import Songs from './containers/Songs';
import GSlide from './containers/GSlide';

export default [
    { path: '/bible', label: 'Bible', icon: IconBible, component: Bible },
    { path: '/songs', label: 'Songs', icon: IconMusic, component: Songs },
    { path: '/gslides', label: 'Google Slides', icon: IconBrandGoogle, component: GSlide },
    { path: '/ppt', label: 'Powerpoint', icon: IconSlideshow, component: Songs },
    { path: '/settings', label: 'Settings', icon: IconSettings, component: Settings },
];
