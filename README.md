# svn.na-mic.org/Dart

Archive of source code originally available at `http://svn.na-mic.org/Dart`

## License

See [Documentation/Manual/Dart.tex](https://github.com/NA-MIC/svn.na-mic.org-Dart/blob/trunk/Documentation/Manual/Dart.tex#L78)

## Migration

1. Clone using `git-svn`

```
git svn clone -s http://svn.na-mic.org/Dart
```

2. Remove `origin/Dart-0.8.5` as it is identical to `origin/tags/Dart-0.8.5`

```
$ cd Dart

$ git diff origin/Dart-0.8.5..origin/tags/Dart-0.8.5

$ git branch -rd origin/Dart-0.8.5
Deleted remote-tracking branch origin/Dart-0.8.5 (was 5e3126f).
```

3. Add remote

```
cd Dart

git remote add origin git@github.com:NA-MIC/svn.na-mic.org-Dart.git
```

3. Create `welcome` branch and add `README.md`

```
git checkout --orphan=welcome

git reset

git rm -rf

echo "# svn.na-mic.org/Dart

Archive of source code originally available at \`http://svn.na-mic.org/Dart\`
" > README.md

git add README.md && git commit -m "Add README"

git push origin welcome
```

5. Push branches and tags

```
# Push branches
for ref in $(git show-ref | grep remotes/origin | grep -v '@' | grep -v 'refs/remotes/origin/welcome' | grep -v remotes/origin/tags | cut -d" " -f2); do
  branch=${ref/refs\/remotes\/origin\//}
  echo "Pushing branch [$branch]"
  git push origin origin/$branch:refs/heads/$branch
done

# Push tags
for ref in $(git show-ref | grep -v '@' | grep remotes/origin/tags | cut -d" " -f2); do
  tag=${ref/refs\/remotes\/origin\/tags\//}
  echo "Pushing tag [$tag]"
  git push origin origin/tags/$tag:refs/tags/$tag
done
```

6. Create `.mailmap`

_Note that the `*` are placeholders. Valid emails address were used._

```
echo "Andy Cedilnik <andy.cedilnik@*******.com> <andy@b2c14c61-d7ef-0310-8715-841e9ab387fe>
Daniel Blezek <Blezek.Daniel@****.edu> <blezek@b2c14c61-d7ef-0310-8715-841e9ab387fe>
Jim Miller <millerjv@**.com> <millerjv@b2c14c61-d7ef-0310-8715-841e9ab387fe>
Amitha Perera <amitha.perera@*******.com> <perera@b2c14c61-d7ef-0310-8715-841e9ab387fe>
"> /tmp/.mailmap
```

7. Use [git-filter-repo](https://github.com/newren/git-filter-repo) to filter names & emails

```
git-filter-repo --mailmap /tmp/.mailmap --force
```

8. Publish updated branches and tags

```
git remote add origin git@github.com:NA-MIC/svn.na-mic.org-Dart.git

# Push branches
for ref in $(git show-ref | grep -v '@' | grep -v 'replace' | grep -v 'refs/heads/tags' | grep -v 'refs/remotes' | grep -v 'refs/heads/welcome' | cut -d" " -f2); do
  branch=${ref/refs\/heads\//}
  echo "Pushing branch [$branch]"
  git push origin refs/heads/$branch:refs/heads/$branch --force
done

# Push tags
for ref in $(git show-ref | grep refs/heads/tags | cut -d" " -f2); do
  tag=${ref/refs\/heads\/tags\//}
  echo "Pushing tag [$tag]"
  git push origin refs/heads/tags/$tag:refs/tags/$tag --force
done
```

