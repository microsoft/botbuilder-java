'use strict';
const Generator = require('yeoman-generator');
const path = require('path');
const _ = require('lodash');
const mkdirp = require('mkdirp');

module.exports = class extends Generator {
  prompting() {
    const prompts = [
      {
        name: 'botName',
        message: `What's the name of your bot?`,
        type: String,
        default: 'echo'
      },
      {
        name: 'packageName',
        message: `What's the fully qualified package name of your bot?`,
        type: String,
        default: 'echo'
      },
      {
        name: 'template',
        type: 'list',
        message: 'What default template do you want?',
        choices: ['echo']
      }
    ];

    return this.prompt(prompts).then(props => {
      // To access props later use this.props.someAnswer;
      this.props = props;
    });
  }

  writing() {
    const botName = this.props.botName;
    const packageName = this.props.packageName.toLowerCase();
    const packageTree = packageName.replace(/\./g, '/');
    const directoryName = _.kebabCase(this.props.botName);
    const defaultDialog = this.props.template.split(' ')[0].toLowerCase();

    if (path.basename(this.destinationPath()) !== directoryName) {
      this.log(`Your bot should be in a directory named ${directoryName}`);
      mkdirp(directoryName);
      this.destinationRoot(this.destinationPath(directoryName));
    }

    // Copy the project tree
    this.fs.copyTpl(
      this.templatePath(path.join(defaultDialog, 'tree', '**')),
      this.destinationPath(),
      {
        botName,
        packageName
      }
    );

    // Copy main source
    this.fs.copyTpl(
      this.templatePath(path.join(defaultDialog, 'src/main/java/**')),
      this.destinationPath(path.join('src/main/java', packageTree)),
      {
        packageName
      }
    );

    // Copy test source
    this.fs.copyTpl(
      this.templatePath(path.join(defaultDialog, 'src/test/java/**')),
      this.destinationPath(path.join('src/test/java', packageTree)),
      {
        packageName
      }
    );
  }
};
