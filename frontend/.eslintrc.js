module.exports = {
    root: true,
    env: {
        node: true,
        browser: true,
        es2021: true
    },
    extends: [
        // Vue2选这个，Vue3替换为 'plugin:vue/vue3-essential'
        'plugin:vue/essential',
        'eslint:recommended'
    ],
    // 核心：仅用vue-eslint-parser解析.vue，内置js解析器（无Babel依赖）
    parser: 'vue-eslint-parser',
    parserOptions: {
        sourceType: 'module',
        ecmaVersion: 2021,
        // 完全删除@babel/eslint-parser相关配置，避免触发Babel依赖
    },
    rules: {
        'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
        'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
        'vue/no-unused-vars': 'warn',
        'no-unused-vars': 'warn',
        // 新增：禁用“组件名必须多单词”的检查规则
        'vue/multi-word-component-names': 'off'
    }
};