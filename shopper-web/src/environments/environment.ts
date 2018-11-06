// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `.angular-cli.json`.

import {NgxLoggerLevel} from "ngx-logger";

export const environment = {
    production: false,
    logging: NgxLoggerLevel.DEBUG,
    default_lang: 'en',
    context: '/shopper',
    api_url: '/api',
    refresh_token_url: '/api/refresh',
    whoami_url: '/api/account/whoami',
    login_url: '/login',
    auth_url: '/auth',
    default_lang_url: '/api/config/default-language',
    logout_url: '/logout',
    resource_url: '/api/resource',
    all_users_url: '/api/account/all',
    account_url: '/api/account',
    avatar_url: '/avatar',
    avatar_upload_url: '/avatar-upload',
    language_url: '/settings/language',
    list_url: '/api/list',
    item_url: '/api/item',
    product_url: '/api/product',
    config_url: '/api/config',
    ws_ur: '/ws',
    ws_send_url: '/ws/send/'
};
