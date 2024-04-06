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

export default [
    { path: '/bible', label: 'Bible', icon: IconBible, component: Bible },
    { path: '/songs', label: 'Songs', icon: IconMusic, component: Songs },
    { path: '/gslides', label: 'Google Slides', icon: IconBrandGoogle, component: Songs },
    { path: '/ppt', label: 'Powerpoint', icon: IconSlideshow, component: Songs },
    { path: '/settings', label: 'Settings', icon: IconSettings, component: Settings },
];
