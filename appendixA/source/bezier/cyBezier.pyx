import math

import warnings
warnings.filterwarnings('error')

if __name__ == '__main__':    
    import matplotlib.lines as lines
    import matplotlib.cm as cm
    from matplotlib.widgets import RadioButtons, Slider
    import matplotlib.pyplot as plt
    import matplotlib
    print matplotlib.__version__
    
    warnings.simplefilter('always', UserWarning)
    
    picked = None
    fig = plt.figure(figsize=(18, 12))
    min_canvas_y = 0.15
    ax = fig.add_axes([0.03, min_canvas_y, 0.95, 0.82])
    
    minimal_fig = plt.figure()
    minimal_ax = minimal_fig.add_subplot(111)
    minimal_ax.set_aspect("equal", "box")
    
draw_all = 'control'

class BezierCurve(object):
    def __init__(self, ga=False):
        self.params = {'fronting': 0.5, 'conc': 0.5, 'angle' : 0.5, 'weigth' : 0.5, 
                       'interval' : 100} # default values
        self.control_points = [[-0.2, 0.6], [0.1, 0.6], [0.2,0.8], [0.6, 0.3], [0.7, 0.3]]
        self.functions = {'fronting': self.update_fronting, 'conc': self.update_conc, 
                          'angle': self.update_angle, 'weigth': self.update_weigth, 
                          'interval': self.update_interval}
        self.ga = ga
        
        if self.ga:
            import numpy as np
            import EA as ea
            self.np = np
            self.ea = ea
       
    """
    Init Bezier calculations
    """
    def calculate(self, update=True):
        spline_points = [] # Bezier spline points
    
        # draw some shit if main
        if __name__ == '__main__':
            ax.cla()
            ax.minorticks_on()
            ax.grid(which='both')
            
            minimal_ax.cla()
            minimal_ax.grid(which='both')
            minimal_ax.xaxis.set_ticklabels([])
            minimal_ax.yaxis.set_ticklabels([])
            
            minimal_ax.text(-0.2, 0.21, r"$\leftarrow$ posterior", fontsize=12)
            minimal_ax.text(0.56, 0.21, r"anterior $\rightarrow$", fontsize=12)

        if update:
            self.update_params()
        
        # initiate Bezier recursion following discretization interval 
        for interval_point in xrange(self.params['interval'] + 1):
            self.spline_point = self.walk_line(self.control_points, interval_point)
            spline_points.append(self.spline_point)
        
        # draw line segments if main
        if __name__ == '__main__':
            for i_line in xrange(len(spline_points) - 1):
                self.draw_line(spline_points, i_line) 
                
            if draw_all != 'spline':
                zpoints = zip(*self.control_points)
                ax.plot(zpoints[0], zpoints[1], marker='o', ls='None', ms=10, mfc='k', picker=10)
                minimal_ax.plot(zpoints[0], zpoints[1], marker='o', ls='None', ms=6, mfc='k')
                 
            self.zoom_and_draw()
                
        return spline_points
    
    def sample(self, intervals=None, n_x=None, min_x=0, max_x=1, min_y=0, max_y=1, update=True):          
        if intervals == None:
            intervals = [i * ((max_x - min_x) / (float(n_x) - 1)) + min_x for i in xrange(n_x)]
        
        #calculate
        spline_points = self.calculate(update=update)           
        
        # pre-rotation
        if self.ga:
            extrema = self.np.array(((spline_points[0][0],spline_points[0][1]),
                                 (spline_points[-1][0],spline_points[-1][1])))   
            (diff_x, diff_y) = extrema[1] - extrema[0]
            angle = -math.atan2(diff_y, diff_x)
            spline_points = self.np.transpose(self.ea.rotate(zip(*spline_points), angle))
            spline_points = list([list(v) for v in spline_points]) # TODO       
        
        # normalize horizontal
        (min_x_ext, max_x_ext) = (intervals[0], intervals[-1])        
        (min_x_spline, max_x_spline) = (spline_points[0][0], spline_points[-1][0])
        
        sample_intervals = []
        for interval in intervals:
            norm_interval = (interval - min_x_ext) / (max_x_ext - min_x_ext)
            norm_interval = norm_interval * (max_x_spline - min_x_spline) + min_x_spline
            sample_intervals.append(norm_interval)      
        
        # sample and interpolate Bezier curve     
        paired_points = zip(spline_points[:-1], spline_points[1:])
        
        samples = []
        while len(sample_intervals) != 0:
            try:
                while paired_points[0][1][0] < sample_intervals[0]:
                    paired_points = paired_points[1:]
                else:
                    (x0,y0) = (paired_points[0][0][0], paired_points[0][0][1])
                    (x1,y1) = (paired_points[0][1][0], paired_points[0][1][1])
                                
                    y = y0 + (y1 - y0) * ((sample_intervals[0]  - x0) / (x1 - x0))
                     
                    samples.append([sample_intervals[0] ,y])
                    sample_intervals = sample_intervals[1:]
            except IndexError:
                samples.append(spline_points[-1])
                sample_intervals = sample_intervals[1:] 
        
        # denormalize vertical
        y_samples = zip(*samples)[1]
        y_results = []
        
        if self.ga:
            (measured_min_y, measured_max_y) = (y_samples[0], max(y_samples))        
        else:
            (measured_min_y, measured_max_y) = (y_samples[-1], y_samples[0])        
        
        for i in xrange(len(intervals)):            
            try:
                normalized = (samples[i][1] - measured_min_y) / (measured_max_y - measured_min_y)
                denormalized = normalized * (max_y - min_y) + min_y
                y_results.append(denormalized)
                
            except Warning:
                print "fuck"
                import sys
                sys.exit()
        
        (xs, ys) = zip(*spline_points) 
        (min_xs, max_xs) = (xs[0], xs[-1])
        norm_xs = [(v - min_xs) * (1 / (max_xs - min_xs)) for v in xs]
        norm_ys = [(v - measured_min_y) * ((1 - min_y) / (measured_max_y - measured_min_y)) + min_y for v in ys]
        
        # check of curve x-points are monotonically increasing (required for VTL rib architecture)
        x_monotonic = all(a <= b for (a,b) in zip(intervals[:-1], intervals[1:]))
        #y_decreasing = denoms[0] > denoms[-1]    
        
        #if not self.ga:
        #    if not x_monotonic or not y_decreasing:
        #        raise ArithmeticError
        #    else:
        #        print 'bla'
        #        return (intervals, denoms)
        #else:
             
        return (intervals, y_results, (norm_xs, norm_ys))
    
    """
    Calculate intermediate point between each pair of points, then recursively calculate
    more intermediate points between those.
    """
    def walk_line(self, line_points, interval_point, depth=1):
        # use recursion as long as we can construct pairs of points
        if len(line_points) > 1:
            #first some drawing shit 
            #c_code = float(depth) / (len(self.control_points) - 1)
            c_code = ((float(depth) - 1) / (len(self.control_points) - 2))
            c_code *= 0.5
            zorder = depth - len(self.control_points) - 1
            
            if __name__ == '__main__':
                color = cm.cubehelix(c_code) # @UndefinedVariable
            else:
                color = None
            
            if depth == 1:
                m_draw = True if draw_all == 'control' else False
            else:
                m_draw = True if draw_all == 'bezier' else False
                       
            # then the actual calculations
            recursion_points = []
            
            for i_line in xrange(len(line_points) - 1):
                (x1,y1,x2,y2) = self.draw_line(line_points, i_line, m_draw, color, zorder)            
                
                dx = (x2 - x1) / self.params['interval']
                dy = (y2 - y1) / self.params['interval']       
                x = x1 + dx * interval_point
                y = y1 + dy * interval_point           
                recursion_points.append([x,y])
                
            # do the same stuff with the newly calculated points and recursively return appended 
            # results (the actual Bezier spline points)
            return self.walk_line(recursion_points, interval_point, depth + 1)
        # otherwise simply return the last point
        else:
            return line_points[0]
    
    """
    Draw line and return x and y values
    """
    def draw_line(self, points, i_line, draw=True, color='k', zorder=0):
        (x1, y1, x2, y2) = (points[i_line][0], points[i_line][1], points[i_line+1][0], points[i_line+1][1])

        if __name__ == '__main__' and draw:
            lw = 4 if zorder == 0 else 0.25
            (line1, line2) = [lines.Line2D([x1, x2],[y1, y2], color=color, zorder=zorder, 
                                           lw=lw if i == 0 else lw / 2.) for i in xrange(2)]
            ax.add_line(line1)
            minimal_ax.add_line(line2)
            
        return (x1,y1,x2,y2)
    
    """
    Update control points once parameters are set
    """
    def update_params(self):
        a_param = self.params['angle']
        c_param = self.params['conc']
        f_param = self.params['fronting']
        w_param = self.params['weigth']
        
        fronting = self.control_points[1][0] + (self.control_points[-1][0] - self.control_points[1][0]) * f_param
        self.control_points[2] = [fronting, self.control_points[1][1] + (self.control_points[0][1] - self.control_points[-1][1]) * c_param]
        
        palate_x = (self.control_points[-1][0] - self.control_points[1][0]) * (1 - a_param)
        palate_x = self.control_points[-1][0] - palate_x * w_param
        
        palate_y = (self.control_points[-1][1] - self.control_points[2][1]) * a_param
        palate_y = self.control_points[-1][1] - palate_y * w_param
    
        self.control_points[3] = [palate_x, palate_y]     
    
    """
    parameter update functions
    """
    def update(self, auto, update_params=True): 
        if auto:
            try:
                self.sample(n_x=15, min_x=0, max_x=4.7, min_y=0, max_y=1.3, update=update_params)
            except ArithmeticError:
                warnings.warn('Curve not usable in VTL (ill-founded function)')
    
    def update_fronting(self, value, auto=True):
        self.params['fronting'] = float(value)
        self.update(auto)
                 
    def update_conc(self, value, auto=True):
        self.params['conc'] = float(value)
        self.update(auto)
        
    def update_angle(self, value, auto=True):
        self.params['angle'] = float(value)
        self.update(auto)
        
    def update_weigth(self, value, auto=True):
        self.params['weigth'] = float(value)
        self.update(auto)
            
    def update_interval(self, value, auto=True):
        value = int(value)
        
        try:
            if __name__ == '__main__' and value % int(value) != 0:
                self.intervalSlider.set_val(int(value))
            else:
                self.params['interval'] = value
                
            self.update(auto)
        except:
            pass
    
    """
    update function for GUI
    """  
    if __name__ == '__main__':
        (boundaries, increments) = ([], [])
        zoom = 'spline'        
              
        def update_show(self, label):
            global draw_all
            draw_all = label
            self.update(auto=True)
            
        def update_zoom(self, label):
            self.zoom = label
            self.zoom_and_draw()
            
        def zoom_and_draw(self):
            if self.zoom == 'fixed':
                ax.axis([-2, 2, -2, 2])
            else:       
                if self.zoom == 'spline':
                    #self.get_axis_increments(self.spline_points)
                    self.get_axis_increments(self.control_points)
                elif self.zoom == 'control':
                    self.get_axis_increments(self.control_points)
                               
                try:
                    int(self.zoom) # fails if not a numerical
            
                    increments = self.increments[:1] * 2 + self.increments[1:] * 2
                    increments = [-v if i % 2 == 0 else v for (i,v) in enumerate(increments)]
                    factor =  (self.zoom - 0.5) * 25        
                    boundaries = [bnd + inc * factor for (bnd, inc) in zip(*(self.boundaries, increments))]
                except:
                    boundaries = self.boundaries
                   
                ax.axis(boundaries)
                
            plt.draw()
            
            #minimal_fig.savefig("d:/bezier.png", dpi=200, bbox_inches='tight') 
            
        """
        calculate axis limits based on points displayed.
        """
        def get_axis_increments(self, points):
            points = (zip(*points)[0], zip(*points)[1])
            extremes = [(min(axis), max(axis)) for axis in points]
            increments = [(axis[1] - axis[0]) * 0.05 for axis in extremes]
            
            self.boundaries = []
            for i in xrange(2):
                for j in xrange(2):            
                    boundary = extremes[i][j] + math.copysign(increments[i], j - 1)
                    self.boundaries.append(boundary)    

"""
Initiate bezier curve
"""
bezier = BezierCurve(ga=False)

"""
public setters and getters
"""
cdef public calculate_curve(double[] data_refs, double[] input_intervals, int size, double min_y, double max_y):
#def calculate_curve(data_refs, input_intervals, size, min_y, max_y):
    intervals = []
    for i in xrange(size):
        intervals.append(input_intervals[i])
       
    try:
        #results = bezier.sample(intervals=intervals, min_y=min_y, max_y=max_y)
        (_, ys, _) = bezier.sample(intervals=intervals, min_y=min_y, max_y=max_y)    
        # python list to c array
        for i in xrange(size):
            #data_refs[i] = results[1][i]
            data_refs[i] = ys[i]
    except ArithmeticError:
        warnings.warn('Curve not usable in VTL (ill-founded function)')
        for i in xrange(size):
            data_refs[i] = -1

cdef public set_fronting(double value):
#def set_fronting(value):
    bezier.update_fronting(value, False)
    
cdef public set_conc(double value):
#def set_conc(value):
    bezier.update_conc(value, False)
    
cdef public set_angle(double value):
#def set_angle(value):
    bezier.update_angle(value, False)
    
cdef public set_weigth(double value):
#def set_weigth(value):
    bezier.update_weigth(value, False)
    
cdef public set_interval(double value):    
#def set_interval(value):
    bezier.update_interval(value, False)
    
"""
finalize
"""
cdef double* c_data
cdef double* c_interval

#if __name__== "__main__":
#if False:
if True:
    from cpython.mem cimport PyMem_Malloc
    
    set_fronting(bezier.params['fronting'])
    set_conc(bezier.params['conc'])
    set_angle(bezier.params['angle'])
    set_weigth(bezier.params['weigth'])
    
    size = 25
    start = 0.2
    end = 4.7

    c_data = <double*> PyMem_Malloc(sizeof(double) * size)
    c_interval = <double*> PyMem_Malloc(sizeof(double) * size)
    #c_data = [0] * size
    #c_interval = [0] * size
    
    for i in xrange(size):
        #c_interval[i] = i * (4.7 / (size - 1))
        c_interval[i] = ((end - start) / (size-1)) * i + start
    
    calculate_curve(c_data, c_interval, size, 0, 1.3)
    for i in xrange(size):
        print str(c_interval[i]) + ',\t' + str(c_data[i]) 
        #print str(c_data[i])

if __name__ == "__main__":    
    axFronting = fig.add_axes([0.22, 0.01, 0.7, 0.01])
    frontingSlider = Slider(axFronting, 'fronting', 0, 1, bezier.params['fronting'])
    frontingSlider.on_changed(bezier.update_fronting)
    
    axPalateConc = fig.add_axes([0.22, 0.025, 0.7, 0.01])
    concSlider = Slider(axPalateConc, 'palate concavity', 0, 1, bezier.params['conc'])
    concSlider.on_changed(bezier.update_conc)
    
    axAngle = fig.add_axes([0.22, 0.04, 0.7, 0.01])
    angleSlider = Slider(axAngle, 'alveolar angle', 0, 1, bezier.params['angle'])
    angleSlider.on_changed(bezier.update_angle)
    
    axWeigth = fig.add_axes([0.22, 0.055, 0.7, 0.01])
    weightSlider = Slider(axWeigth, 'palatal weigth', 0, 1, bezier.params['weigth'])
    weightSlider.on_changed(bezier.update_weigth)
    
    axInterval = fig.add_axes([0.22, 0.1, 0.7, 0.01])
    intervalSlider = Slider(axInterval, 'smoothness', 0, 100, valinit=bezier.params['interval'], color='r')
    intervalSlider.on_changed(bezier.update_interval)
    
    axShow = fig.add_axes([0.02, 0.02, 0.05, 0.09])
    showRadio = RadioButtons(axShow, ('bezier', 'control', 'spline'), active=1)
    showRadio.on_clicked(bezier.update_show)
    
    axZoom = fig.add_axes([0.075, 0.02, 0.05, 0.09])
    zoomRadio = RadioButtons(axZoom, ('control', 'spline', 'fixed'), active=1)
    zoomRadio.on_clicked(bezier.update_zoom)        
    
    # event handling functions
    def onpick(pickEvent):
        global picked
        picked = pickEvent.ind
    
    def onpress(mouseEvent):
        global picked
        if picked == None:
            point = [mouseEvent.xdata, mouseEvent.ydata]
            max_y = fig.canvas.get_width_height()[1] * min_canvas_y           
            if point != [None, None] and mouseEvent.y > max_y:
                bezier.control_points.append(point)
    
    def onrelease(mouseEvent):
        global picked
        if picked != None:
            bezier.control_points[picked] = (mouseEvent.xdata, mouseEvent.ydata)         
        picked = None
        
        point = [mouseEvent.xdata, mouseEvent.ydata]
        max_y = fig.canvas.get_width_height()[1] * min_canvas_y

        if point != [None, None] and mouseEvent.y > max_y:
            bezier.update(auto=True, update_params=False)

    fig.canvas.mpl_connect('button_release_event', onrelease)
    fig.canvas.mpl_connect('button_press_event', onpress)
    fig.canvas.mpl_connect('pick_event', onpick)      
            
    plt.show()