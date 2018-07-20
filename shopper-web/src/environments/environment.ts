// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `.angular-cli.json`.

export const environment = {
    production: false,
    context: '',
    api_url: '/api',
    refresh_token_url: '/api/refresh',
    whoami_url: '/api/account/whoami',
    login_url: '/api/login',
    logout_url: '/api/logout',
    resource_url: '/api/resource',
    all_users_url: '/api/account/all'
};
