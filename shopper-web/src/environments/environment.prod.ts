import {NgxLoggerLevel} from "ngx-logger";
import packageInfo from "../../package.json";


export const environment = {
    production: true,
    logging: NgxLoggerLevel.OFF,
    default_lang: 'en',
    context: '/shopper',
    api_url: '/api',
    refresh_token_url: '/api/refresh',
    whoami_url: '/api/account/whoami',
    login_url: '/login',
    auth_url: '/auth',
    oauth_login_url: '/oauth2/authorize/',
    default_lang_url: '/api/config/default-language',
    logout_url: '/logout',
    resource_url: '/api/resource',
    all_users_url: '/api/account/all',
    avatar_upload_url: '/avatar-upload',
    language_url: '/settings/language',
    rightmode_url: '/settings/rightmode',
    favorites_sorting_url: '/settings/favorites',
    devices_url: '/settings/devices',
    account_url: '/api/account',
    avatar_url: '/avatar',
    list_url: '/api/list',
    item_url: '/api/item',
    product_url: '/api/product',
    config_url: '/api/config',
    ws_ur: '/ws',
    ws_send_url: '/ws/send/',
    version: packageInfo.version,
};
