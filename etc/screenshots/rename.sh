pushd $1
for screen in composer dictionary favorites rhymer settings thesaurus
do
  mv $screen-*.png $screen.png
done
popd
