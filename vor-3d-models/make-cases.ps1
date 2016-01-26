# Vör .stl model batch render
# Change the file extension and premissions if needed for your command shell
# Prepare to be underwhelmed by the speed...

#----------------- Logo

echo
Get-Date
echo "Render NEGATIVE SPACE logo"
openscad -o vor-logo-space.stl -D 'mode="""space"""' vor-logo.scad

echo
Get-Date
echo "Render PLATE logo"
openscad -o vor-logo-plate.stl -D 'mode="""plate"""' vor-logo.scad

echo
Get-Date
echo "Render EMBOSSED logo"
openscad -o vor-logo-embossed.stl -D 'mode="""embossed"""' vor-logo.scad

echo
Get-Date
echo "Render LITHOPHANE logo"
openscad -o vor-logo-lithophane.stl -D 'mode="""lithophane"""' vor-logo.scad

echo
Get-Date
echo "Render LITHOPHANE TESTBLOCK logo"
openscad -o vor-logo-lithophane-lithophane-testblock.stl -D 'mode="""testblock"""' vor-logo.scad

echo
Get-Date
echo "Render plain logo"
openscad -o vor-logo-lithophane-lithophane-testblock.stl vor-logo.scad

#------------------ Toggle Bit Case

echo
Get-Date
echo "Render left toggle bit case"
openscad -o toggle-bit-case-left.stl -D 'mode="""left"""' toggle-bit-case.scad

echo
Get-Date
echo "Render right toggle bit case"
openscad -o toggle-bit-case-right.stl -D 'mode="""right"""' toggle-bit-case.scad
