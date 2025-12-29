#!/bin/bash

echo "🔄 Updating master branch..."
git checkout master
git pull origin master

echo "🗑️ Deleting old dev branch..."
git branch -D dev

echo "✨ Creating new dev branch from master..."
git checkout -b dev

echo "🚀 Pushing new dev branch (force push)..."
git push origin dev --force

echo "✅ Done! The dev branch is clean and in sync with master."